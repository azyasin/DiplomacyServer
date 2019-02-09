package bandana.gameServer;

import bandana.gameServer.tools.timer.Timer;

import java.util.*;


/**
 * This is the main class. When the user starts the server it starts by running the main method of this class.
 *
 * @author Dave de Jonge, Western Sydney University
 */
public class BandanaServer {
	//TODO: this class should be implemented such that the user can set the deadlines, the final year, the number of games and 
	// one of two 'modes' to assign powers to players: either randomly, or 'egalitarian'.


	//Furthermore, if the user wants any different kind of behavior he/she should be able to extend this class to implement 
	// the desired behavior.

	//STATIC FIELDS
	static boolean PDA = true;
	static boolean NPR = false;
	static boolean NPB = false;
	static int PTL = 0;
	static boolean egalitarian = true;
	static int moveTimeLimit = 30;
	static int retreatTimeLimit = 5;
	static int buildTimeLimit = 5;
	static int level = 10;

	//STATIC METHODS
	public static void main(String[] args) {

		//By default, the number of games to run will be 1. The user may specify through the command line arguments that more games must be played.
		int numberOfGames = 1;
		if (args.length >= 1) {
			numberOfGames = Integer.parseInt(args[0]);
			List<String> options = new ArrayList<>(Arrays.asList(args));
			int index = options.indexOf("-l");
			if (index > -1) {
				if (options.get(index + 1).equals("OBS")) {
					level = 0;
				}
				else {
					level = Integer.parseInt(options.get(index + 1));
				}
			}
			index = options.indexOf("-MTL");
			if (index > -1) {
				moveTimeLimit = Integer.parseInt(options.get(index + 1));
			}
			index = options.indexOf("-RTL");
			if (index > -1) {
				retreatTimeLimit = Integer.parseInt(options.get(index + 1));
			}
			index = options.indexOf("-BTL");
			if (index > -1) {
				buildTimeLimit = Integer.parseInt(options.get(index + 1));
			}
			index = options.indexOf("-PTL");
			if (index > -1) {
				PTL = Integer.parseInt(options.get(index + 1));
			}
			if (options.contains("-PDA")) {
				PDA = true;
			}
			else {
				PDA = false;
			}

			if (options.contains("-NPR")) {
				NPR = true;
			}
			else {
				NPR = false;
			}

			if (options.contains("-NPB")) {
				NPB = true;
			}
			else {
				NPB = false;
			}

			if (options.contains("-R")) {
				egalitarian = false;
			}
			else {
				egalitarian = true;
			}
		}

		ServerLogger.CreateFiles();



		//Create an instance of the server and run it
		BandanaServer bandanaServer = new BandanaServer(numberOfGames);
		bandanaServer.runTournament();
		System.out.println("Tournament over");
		return;
	}


	//FIELDS

	/**
	 * The total number of games to play in the current tournament (including games that have already finished)
	 */
	private int numGamesToPlay;

	/**
	 * The number of games that have already finished.
	 */
	private int numGamesPlayed = 0;

	/**
	 * The port at which the server is listening.
	 */
	private int port = Constants.DEFAULT_PORT;  //TODO: allow the user to set the port via the command line arguments.


	/**
	 * The Listener object that will be listening for incoming connections.
	 */
	private Listener listener = null;


	//CONSTRUCTORS
	public BandanaServer(int numGamesToPlay) {
		this.numGamesToPlay = numGamesToPlay;
	}


	//METHODS
	final public void runTournament() {
		ServerLogger logger = new ServerLogger();
		List<String> powerAssignment = null;

		//Start a clock that ticks every 200 ms. The GameManagers will use this clock to check deadlines.
		Timer timer = new Timer(200);
		timer.start(); //start the timer in a separate thread.

		for (int i = 0; i < this.numGamesToPlay; i++) {
			//Create a new GameManager object.
			GameManager gameManager = new GameManager(this.getInfoNextGame(), logger);

			//Make sure the new GameManager is notified of timer ticks.
			timer.getTimerTickHandlers().add(gameManager);

			//Make sure we have listener running to listen for incoming connections.
			if (listener == null) {
				listener = new Listener(port, gameManager);
				listener.start();
			} else {
				listener.setGameManager(gameManager); //add the GameManager to the listener, so that the listener can pass new ConnectionHandlers to the GameManager.
			}

			//wait until 7 players have connected. (every time a player connects the Listener will pass a ConnectionHandler to the Game Manager)
			while (gameManager.getConnectedPlayerNames().size() != 7) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					ServerLogger.LogError(e.toString());
				}
			}
			System.out.println("Connected 7 Players");

			//assign powers to the players.
			powerAssignment = assignPowers(powerAssignment, gameManager.isEgalitarian());

			//inform the gameManager of this power assignment
			gameManager.setPowerAssignment(powerAssignment);

			//wait for all players to be ready
			while (!gameManager.allPlayersReady()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ServerLogger.LogError(e.toString());
				}
			}

			gameManager.startGame();

			//Wait until game is finished.
			while (!gameManager.gameIsFinished()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					ServerLogger.LogError(e.toString());
				}
			}

			//Once the game has finished, make sure the GameManager no longer receives the timer notifications.
			//gameManager.closeAllConnections();
			timer.getTimerTickHandlers().remove(gameManager);
		}
		listener.stopListening();
		timer.cancel();
		logger.closeLogger();
		logger = null;
	}

	//GETTERS AND SETTERS

	/**
	 * Returns the info for the next game to play.
	 *
	 * @return
	 */
	protected GameInfo getInfoNextGame() {

		String gameID = "Game " + (numGamesPlayed + 1);
		GameInfo gameInfo = new GameInfo(gameID);
		gameInfo.setBuildTimeLimit(buildTimeLimit);
		gameInfo.setEgalitarian(egalitarian);
		gameInfo.setMoveTimeLimit(moveTimeLimit);
		gameInfo.setRetreatTimeLimit(retreatTimeLimit);
		gameInfo.setPTL(PTL);
		gameInfo.setLevel(level);
		gameInfo.setPDA(PDA);
		gameInfo.setNPB(NPB);
		gameInfo.setNPR(NPR);
		return gameInfo;
	}


	/**
	 * Once 7 players have connected this method is called with the names of the 7 players.<br/>
	 * It will return an assignment of powers to these players.
	 *
	 * @return
	 */
	protected List<String> assignPowers(List<String> assignment, boolean egalitarian) {
		if (assignment != null && egalitarian) {
			Collections.rotate(assignment, 1);
			return assignment;
		}

		List<String> powers = new ArrayList<>();
		String[] power = new String[]{"AUS", "ENG", "FRA", "GER", "ITA", "RUS", "TUR"};
		Collections.shuffle(Arrays.asList(power));
		for (String s: power) {
			powers.add(s);
		}
		return powers;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
