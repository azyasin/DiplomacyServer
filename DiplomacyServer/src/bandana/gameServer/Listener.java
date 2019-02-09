package bandana.gameServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This class does nothing but listening for incoming connections. Every time a client connects, it creates a ConnectionHandler and passes that connectionHandler to the GameManager. <br/>
 * The GameManager is the object that will be handling the game logic.
 *
 * @author Dave de Jonge, Western Sydney University
 */

public class Listener extends Thread {


	//FIELDS
	GameManager gameManager;

	private ServerSocket serverSocket = null;
	private boolean listening = false;
	private int port;

	//CONSTRUCTOR
	public Listener(int port, GameManager gameManager) {
		this.port = port;
		this.gameManager = gameManager;
	}

	public void setGameManager(GameManager gameManager) {
		this.gameManager = gameManager;

	}

	@Override
	public void run() {

		System.out.println("Server started. Waiting for client to connect.");

		listening = true;

		//1. Create the socket used to listen for incoming connections.
		try {
			serverSocket = new ServerSocket();
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(this.port));

			//System.out.println(serverSocket);

		} catch (IOException e) {
			//something went wrong creating the ServerSocket....
			listening = false;
			e.printStackTrace();
			return;
		}


		//2. Start listening..
		try {
			while (true) { //When another thread closes the socket (by calling stopListening()), the method serverSocket.accept() will throw an exception and therefore we will break out of this loop.
				Socket socket = serverSocket.accept(); //wait until we have an incoming connection.
				socket.setKeepAlive(true);

				//Once a client has connected, create a ConnectionHandler object and pass it to the GameManager. After this we can continue listening for more clients.
				//The ConnectionHandler will receive all the incoming messages from the client and will pass them on to the GameManager.
				synchronized (this) {
					ConnectionHandler connectionHandler = new ConnectionHandler(gameManager, socket);
					gameManager.addConnectionHandler(connectionHandler);
					connectionHandler.start(); //Start a new thread to receive the incoming messages.
				}

				//continue listening for more clients...

			}

		} catch (IOException e) {
			//the server socket was closed.
		}

		listening = false;
		System.out.println("Server shutting down.");
	}


	/**
	 * Call this method to stop the server.
	 */
	void stopListening() {
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isListening() {
		return listening;
	}
}