package bandana.gameServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.regex.Pattern;


/**
 * Handles the connection between the server and a single client. All received messages are passed to the GameManager which will analyze them and generate the appropriate response.
 *
 * @author Dave de Jonge
 */
public class ConnectionHandler extends Thread {

	//STATIC FIELDS

	//STATIC METHODS

	//FIELDS
	private GameManager gameManager;

	private Socket clientSocket;
	private boolean connected = false;


	private String clientName = "";
	private String clientVersion = "";
	private String powerName = "";

	private boolean isPlayer = false;
	//'false' means that the client is an observer, 'true' means the client is a player. Each client is considered an observer by default, until the server receives an NME message from the client.
	private boolean isRecieving = false;
	private boolean isEliminated = false;
	private int eliminationYear = 0;

	private InputStream inputStream;  //for receiving messages from the client
	private OutputStream outputStream;    //for sending messages to the client.
	private boolean tme = true;
	private boolean ready = false;

	//CONSTRUCTORS
	ConnectionHandler(GameManager gameManager, Socket clientSocket) {
		this.gameManager = gameManager;
		this.clientSocket = clientSocket;
		this.isRecieving = false;
		try {
			inputStream = clientSocket.getInputStream();
			outputStream = clientSocket.getOutputStream();
		} catch (Exception e) {
			gameManager.serverLogger.LogError(e.getMessage());
		}
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String name) {
		this.clientName = name;
	}

	public String getPowerName() { return powerName; }

	public void setPowerName(String name) { this.powerName = name; }

	public void setIsPlayer(boolean isPlayer) { this.isPlayer = isPlayer; }

	public boolean isPlayer() {
		return isPlayer;
	}

	public boolean isRecieving() {
		return isRecieving;
	}

	public void isRecieving(boolean isReceiving) {
		this.isRecieving = isReceiving;
	}

	public boolean isEliminated() {
		return isEliminated;
	}

	public void setEliminated(boolean eliminated) {
		System.out.println(powerName + "  is eliminated");
		isEliminated = eliminated;
	}

	public int getEliminationYear() {
		return eliminationYear;
	}

	public void setEliminationYear(int eliminationYear) {
		this.eliminationYear = eliminationYear;
	}

	public boolean isTme() {
		return tme;
	}

	public void setTme(boolean tme) {
		this.tme = tme;
	}

	public String getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public boolean isReady() {
		return ready;
	}

	//METHODS

	@Override
	public void run() { //When calling ConncectionHandler.start()  the Java Virtual Machine will start a new thread and call this method in that new thread.
		try {

			connected = true;
			System.out.println("Client connected on port " + clientSocket.getLocalPort() + " and thread " + currentThread().getId());


			//Receive Initial Message
			Message im = readMessage(inputStream, gameManager);
			//Send Representation Message
			outputStream.write(new byte[]{1, 0, 0, 0});

			if (im.messageType != 0) {
				//Send error Message
				outputStream.write(new byte[]{4, 0, 0, 2, 0,1});
				System.out.println("Initial message is not an IM. Connection closed.");
				connected = false;
				return;
			}

			if (!Pattern.matches("[a-zA-Z0-9]{0,6}da|DA", Integer.toHexString(im.messageContent[2])) && im.messageContent[2] != 10) {
				//Send error Message
				outputStream.write(new byte[]{4, 0, 0, 2, 0,1});
				System.out.println("Incorrect version number or magic number. Connection closed.");
				for (byte b : im.messageContent) {
					System.out.print(Integer.toHexString(b) + " ");
				}
				System.out.println();
				System.out.println(im.messageContent.length);
				connected = false;
				return;
			}

			while (true) {

				//Receive message.
				Message message = readMessage(inputStream, gameManager);
				if (message == null){
					gameManager.removeConnectionHandler(this);
					gameManager.removePlayerHandler(this);
					if (!gameManager.gameIsFinished() && !gameManager.drawUnanimouslyAccepted()) {
						if (gameManager.isGameStarted()) {
							gameManager.addCivilDisorder(gameManager.game.getPower(powerName));
						}
					}
					break;
				}

				//pass the message to the GameManager
				gameManager.receiveMessage(message, this);


				//when the connection is closed (either by the server or by the client) an exception will be thrown and we will
				// break out of this loop.
			}
		} catch (IOException e) {
			gameManager.serverLogger.LogError(clientName + " " + e.getMessage());
		}

		connected = false;
		closeConnection();
		System.out.println("Connection closed");
	}

	/**
	 * Wait for a new message from the client, and return it as a Message object.
	 */
	private static Message readMessage(InputStream inputStream, GameManager gameManager) throws IOException {
		//read message header:
		byte[] msgHeader = new byte[4]; //header is always 4 bytes
		inputStream.read(msgHeader); //hangs until the client sends something.

		//get the type of the message
		byte msgType = msgHeader[0];
		if (msgType == 3) {
			try {
				throw new RuntimeException("Client has sent final message. Closing connection.");
			} catch (Exception e) {
				gameManager.serverLogger.LogError(e.getMessage());
			}
			return null;
		}

		//calculate how many bytes are remaining.
		int remainingBytes = 256 * (msgHeader[2] & 0xff) + (msgHeader[3] & 0xff);

		//read message body.
		byte[] msgBody = null;
		if (remainingBytes > 0) {
			msgBody = new byte[remainingBytes];
			inputStream.read(msgBody);
			return new Message(msgType, msgBody);
		}
		else {
			try {
				throw new RuntimeException("Unable to read from client. Closing connection.");
			} catch (Exception e) {
				gameManager.serverLogger.LogError(e.getMessage());
			}
			return null;
		}
	}

	public void sendMessage(Message msg, ServerLogger serverLogger) {
		try {
			String hexLen = Integer.toHexString(msg.messageContent.length);
			byte[] msgHead = new byte[4];
			msgHead[0] = msg.messageType;
			msgHead[1] = 0;

			if (msg.messageContent.length < 256) {
				msgHead[2] = 0x00;
				msgHead[3] = (byte) msg.messageContent.length;
			}
			else if (msg.messageContent.length == 256) {
				msgHead[2] = 0x01;
				msgHead[3] = 0x00;
			}
			else {
				int div = msg.messageContent.length / 256;
				int rem = msg.messageContent.length - (div * 256);
				msgHead[2] = (byte) div;
				msgHead[3] = (byte) rem;
			}

			/*System.out.print("Sending Header: ");
			for (int i = 0; i < 4; i++){
				System.out.print(Integer.toHexString(msgHead[i] & 0xff) + " ");
			}*/
			//System.out.println();
			//System.out.println("Sending: " + msg.toString());
			serverLogger.LogMessage("Server -> " + clientName + ": " + msg.toString());
			outputStream.write(msgHead);
			outputStream.write(msg.messageContent);

		}
		catch (Exception e) {
			gameManager.serverLogger.LogError(clientName + " " + e.getMessage());
			gameManager.removePlayerHandler(this);
			gameManager.removeConnectionHandler(this);
		}
	}

	public Thread getThread() { return Thread.currentThread(); }

	//GETTERS AND SETTERS
	public boolean isConnected() {
		return connected;
	}

	void closeConnection() {
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			gameManager.serverLogger.LogError(clientName + " " + e.getMessage());
		}
	}
}
