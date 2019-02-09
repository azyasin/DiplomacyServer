package bandana.gameServer;

import java.util.*;
import bandana.gameServer.tools.timer.TimerTickHandler;
import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import ddejonge.bandana.internalAdjudicator.InternalAdjudicator;
import es.csic.iiia.fabregues.dip.board.*;
import es.csic.iiia.fabregues.dip.comm.daide.Conn;
import es.csic.iiia.fabregues.dip.orders.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is the class that executes all the 'logic' involving a single game of Diplomacy.<br/>
 * The server creates a new instance of this class for every new game.<br/>
 * <br/>
 * NOTE: we are currently assuming that for each new game the players and observers have to reconnect to the server.
 * We may need to change this in the future, but for now we can leave it like this.
 *
 * @author Dave de Jonge, Western Sydney University
 */
public class GameManager implements TimerTickHandler {

	//STATIC FIELDS
	//STATIC METHODS

	//FIELDS
	private List<ConnectionHandler> observerConnections;
	private List<ConnectionHandler> playerConnections;
	private HashMap<Region, Order> submittedOrders;
	private HashMap<String, List<ConnectionHandler>> acceptingDraw;
	private HashMap<String, List<Message>> orderHistory;
	private HashMap<Power, HLOLogin> hloHashMap;
	private HashMap<ConnectionHandler, Boolean> acceptingGOF;
	private List<WVEOrder> wveOrders;
	private List<Power> civilDisorder;
	private boolean resolvingRound;
	Game game;
	private InternalAdjudicator adjudicator;
	ServerLogger serverLogger;
	private long deadLine;
	private long pressDeadline;
	private boolean gameStarted;
	private boolean wrappingUp;
	private GameHistory gameHistory;

	/**
	 * Contains the parameters of the game managed by this  <br/>
	 * e.g. the deadlines, and the year after which a draw will be declared.
	 */
	private GameInfo gameInfo;

	//CONSTRUCTORS
	public GameManager(GameInfo gameInfo, ServerLogger serverLogger) {
		observerConnections = new ArrayList<>();
		playerConnections = new ArrayList<>();
		submittedOrders = new HashMap<>();
		acceptingDraw = new HashMap<>();
		this.gameInfo = gameInfo;
		game = DiplomacyGameBuilder.createDefaultGame();
		adjudicator = new InternalAdjudicator();
		deadLine = 0;
		pressDeadline = 0;
		orderHistory = new HashMap<>();
		hloHashMap = new HashMap<>();
		acceptingGOF = new HashMap<>();
		this.serverLogger = serverLogger;
		civilDisorder = new ArrayList<>();
		resolvingRound = false;
		wveOrders = new ArrayList<>();
		gameStarted = false;
		wrappingUp = false;
		gameHistory = new GameHistory(gameInfo);
	}

	//METHODS

	//GETTERS AND SETTERS

	//Is called by the Listener whenever a new client connects. The listener creates a ConnectionHandler to handle the connection with that client, and passes it
	// to the GameManager via this method.
	synchronized void addConnectionHandler(ConnectionHandler connectionHandler) {
		this.observerConnections.add(connectionHandler); //by default each new connection is initially treated as an observer. Once the client sends a NME message, the connection can be removed from observerConnections and be added to playerConnections.
	}

	synchronized void removeConnectionHandler(ConnectionHandler connectionHandler) {
		this.observerConnections.remove(connectionHandler);
	}

	synchronized void addPlayerHandler(ConnectionHandler connectionHandler) {
		this.playerConnections.add(connectionHandler); //by default each new connection is initially treated as an observer. Once the client sends a NME message, the connection can be removed from observerConnections and be added to playerConnections.
	}

	synchronized void removePlayerHandler(ConnectionHandler connectionHandler) {
		this.playerConnections.remove(connectionHandler);
	}


	synchronized void receiveMessage(Message message, ConnectionHandler sender) {
		if (message == null) {
			return;
		}

		if (message.messageContent.length > 0) {
			MessageHandler.handleMessage(message, this, sender, gameInfo);
		}
	}

	/**
	 * This method is called by the server at regular intervals, before the game has started.
	 * Once it returns a list with 7 players, the server will assign powers to the players and will call the method setPowerAssignment.
	 */
	synchronized List<String> getConnectedPlayerNames() {
		ArrayList<String> names = new ArrayList<>();
		for (ConnectionHandler i : playerConnections) {
			names.add(i.getClientName());
		}
		return names;
	}

	/**
	 * After all 7 players have connected the server calls this method to set the power assignment.
	 * Next, the GameManager should start the game.
	 *
	 * @param powerAssignment
	 */
	synchronized void setPowerAssignment(List<String> powerAssignment) {
		Random random = new Random();
		for (int i = 0; i < 7; i++) {
			playerConnections.get(i).setPowerName(powerAssignment.get(i));
			int passcode = random.nextInt(8190);
			HLOLogin hloLogin = new HLOLogin(game.getPower(powerAssignment.get(i)), passcode,
					playerConnections.get(i).getClientName(), playerConnections.get(i).getClientVersion());
			hloHashMap.put(game.getPower(playerConnections.get(i).getPowerName()), hloLogin);
		}
	}

	synchronized boolean gameIsFinished() {
		if (wrappingUp) {
			return true;
		}
		if (drawUnanimouslyAccepted() && acceptingDraw.size() > 0) {
			sendToAllConnectionHandlers(ResponseBuilder.DRW());
			sendToAllConnectionHandlers(ResponseBuilder.SMR(game, playerConnections));
			wrappingUp = true;
			return true;
		}
		if (game.getYear() == gameInfo.getFinalYear()) {
			sendToAllConnectionHandlers(ResponseBuilder.DRW());
			sendToAllConnectionHandlers(ResponseBuilder.SMR(game, playerConnections));
			wrappingUp = true;
			return true;
		}
		for (Power power : game.getPowers()) {
			if (power.getOwnedSCs().size() >= 18) {
				sendToAllConnectionHandlers(ResponseBuilder.SLO(power));
				sendToAllConnectionHandlers(ResponseBuilder.SMR(game, playerConnections));
				wrappingUp = true;
				return true;
			}
		}
		return false;
	}


	/**
	 * This method is automatically called by the Timer at regular intervals.
	 */
	@Override
	synchronized public void handleTimerTick() {
		//get the current time.
		long currentTime = System.currentTimeMillis();
		if (deadLine != 0 && !(gameInfo.isDSD() && civilDisorder.size() > 0)) {
			if (currentTime >= deadLine && !gameIsFinished()) {
				resolveRound();
			}
		}
		if (isAcceptingGOF()) {
			if (game.getPhase() == Phase.WIN) {
				//System.out.println((subSize + wveOrders.size()) + " " + getExpectedOrders());
				if ((submittedOrders.size() + wveOrders.size()) == getExpectedOrders() && !gameIsFinished()) {
					resolveRound();
				}
			} else {
				if (submittedOrders.size() == getExpectedOrders() && !gameIsFinished()) {
					resolveRound();
				}
			}
		}
	}

	synchronized public void sendNOWandSCO(boolean sendSCO) {
		Message SCO = ResponseBuilder.SCO(game.getPowers(), game.getProvinces());
		Message NOW = ResponseBuilder.NOW(game.getPowers(), game.getYear(), game.getPhase(), game);
		sendToAllConnectionHandlers(SCO);
		sendToAllConnectionHandlers(NOW);
		List<Message> messages = orderHistory.get(new Turn(game.getYear(), game.getPhase()));
		if (messages == null) {
			messages = new ArrayList<>();
		}
		messages.add(SCO);
		messages.add(NOW);
		String key = Integer.toString(game.getYear()) + game.getPhase().name();
		orderHistory.put(key, messages);
	}

	synchronized public void setNextDeadLine() {
		switch (game.getPhase()) {
			case SUM:
			case AUT:
				deadLine = System.currentTimeMillis() + gameInfo.getRetreatTimeLimit() * 1000;
				if (!gameInfo.isNPR()) {
					if (gameInfo.getPTL() <= 0) {
						pressDeadline = deadLine;
					} else {
						pressDeadline = deadLine - (gameInfo.getPTL() * 1000);
					}
				} else {
					pressDeadline = 0;
				}
				break;
			case FAL:
			case SPR:
				deadLine = System.currentTimeMillis() + gameInfo.getMoveTimeLimit() * 1000;
				if (gameInfo.getPTL() <= 0) {
					pressDeadline = deadLine;
				} else {
					pressDeadline = deadLine - (gameInfo.getPTL() * 1000);
				}
				break;
			case WIN:
				deadLine = System.currentTimeMillis() + gameInfo.getBuildTimeLimit() * 1000;
				if (!gameInfo.isNPB()) {
					if (gameInfo.getPTL() <= 0) {
						pressDeadline = deadLine;
					} else {
						pressDeadline = deadLine - (gameInfo.getPTL() * 1000);
					}
				} else {
					pressDeadline = 0;
				}
				break;
			default:
				break;
		}
	}

	synchronized long getDeadLine() {
		return deadLine;
	}

	synchronized long getPressDeadLine() {
		return pressDeadline;
	}

	synchronized public void resolveRound() {
		if (gameIsFinished()) {
			return;
		}
		resolvingRound = true;
		boolean SCO = true;
		List<Order> ordersForAllPowers = new ArrayList<>();
		List<Message> ORDs = new ArrayList<>();
		checkForCivilDisorders();
		Phase phase = game.getPhase();
		if (phase == Phase.FAL || phase == Phase.SPR) {
			addMissingHLDOrders();
		}
		if (phase == Phase.SUM || phase == Phase.AUT) {
			addMissingDSBOrders();
		}
		if (phase == Phase.WIN) {
			addMissingWVEOrders();
		}
		submittedOrders.forEach((k, v) -> ordersForAllPowers.add(v));
		ordersForAllPowers.addAll(wveOrders);

		if (phase == Phase.WIN) {
			for (Order o : ordersForAllPowers) {
				if (o instanceof BLDOrder) {
					processSuccessfulBLDOrder(o);
				}
				if (o instanceof REMOrder) {
					processSuccessfulREMOrder(o);
				}
				ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "SUC", false));
			}
		} else {
			adjudicator.clear();
			adjudicator.resolve(game, ordersForAllPowers);
			for (Order o : ordersForAllPowers) { //for all orders submitted
				if (adjudicator.getResult(o)) {
					if (o instanceof MTOOrder) {
						processSuccessfulMTOOrder(o);
					}
					if (o instanceof RTOOrder) {
						processSuccessfulRTOOrder(o);
					}
					ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "SUC", false));
				} else if (o instanceof DSBOrder) {
					List<Region> regionList = game.getPower(o.getPower().getName()).getControlledRegions();
					Province province = o.getLocation().getProvince();
					game.getPower(o.getPower().getName()).resetControl();
					for (Region region : regionList) {
						if (!region.equals(o.getLocation())) {
							game.getPower(o.getPower().getName()).addControlledRegion(region);
						}
					}
					if (province.isSC()) {
						List<Province> provinceList = game.getPower(o.getPower().getName()).getOwnedSCs();
						game.getPower(o.getPower().getName()).resetOwn();
						for (Province province1 : provinceList) {
							if (!province1.equals(province)) {
								game.getPower(o.getPower().getName()).addOwn(province1);
							}
						}
					}
				}
			}
			for (Order o : ordersForAllPowers) {
				if (!adjudicator.getResult(o)) {
					ORDs = addFailedOrdersToORDList(o, ORDs, ordersForAllPowers);
				}
			}
		}
		//printOrderList(ordersForAllPowers, adjudicator);
		String thisTurn = Integer.toString(game.getYear()) + game.getPhase().name();
		String keyTurn = "";
		for (String turn : orderHistoryKeys()) {
			if (turn.equals(thisTurn)) {
				keyTurn = turn;
			}
		}
		List<Message> messages = orderHistory.get(keyTurn);
		if (messages == null) {
			messages = new ArrayList<>();
		}
		for (Message message : ORDs) {
			sendToAllConnectionHandlers(message);
			messages.add(message);
		}
		String key = Integer.toString(game.getYear()) + game.getPhase().name();
		orderHistory.put(key, messages);
		if (phase == Phase.WIN) {
			SCO = false;
		}
		if (phase == Phase.SUM || phase == Phase.AUT) {
			removeKilledRegions();
		}

		updatePlayerStatuses();
		gameHistory.addGameLoggingState(new GameLoggingState(game, civilDisorder, orderHistory));
		changeYearandPhase();
		submittedOrders.clear();
		wveOrders.clear();
		sendNOWandSCO(SCO);
		resetAcceptingDraw();
		resetAcceptingGOF();
		resolvingRound = false;
		setNextDeadLine();
	}

	synchronized void removeKilledRegions() {
		HashMap<Region, Dislodgement> dislodgementHashMap = game.getDislodgedRegions();
		Set<Region> regionSet = dislodgementHashMap.keySet();
		for (Region region : regionSet) {
			for (Power power : game.getPowers()) {
				if (power.isControlling(region)) {
					List<Region> currentRegions = power.getControlledRegions();
					game.getPower(power.getName()).resetControl();
					for (Region region1 : currentRegions) {
						if (!region.equals(region1)) {
							game.getPower(power.getName()).addControlledRegion(region1);
						}
					}
					List<Province> currentProvinces = power.getOwnedSCs();
					game.getPower(power.getName()).resetOwn();
					for (Province province : currentProvinces) {
						if (!province.equals(region.getProvince())) {
							game.getPower(power.getName()).addOwn(province);
						}
					}
				}
			}
		}
		dislodgementHashMap.clear();
	}

	synchronized void changeYearandPhase() {
		if (game.getPhase().equals(Phase.SPR)) {
			game.setPhase(Phase.SUM);
		} else if (game.getPhase().equals(Phase.SUM)) {
			game.setPhase(Phase.FAL);
		} else if (game.getPhase().equals(Phase.FAL)) {
			game.setPhase(Phase.AUT);
		} else if (game.getPhase().equals(Phase.AUT)) {
			game.setPhase(Phase.WIN);
		} else {
			game.setPhase(Phase.SPR);
			game.setYear(game.getYear() + 1);
		}
	}

	synchronized HashMap<Region, Order> getSubmittedOrders() {
		return submittedOrders;
	}

	synchronized Order getOrderForRegion(Region region) {
		return submittedOrders.get(region);
	}

	synchronized void addSubmittedOrder(Order order) {
		submittedOrders.put(order.getLocation(), order);
	}

	synchronized void removeSubmittedOrder(Region region) {
		submittedOrders.remove(region);
	}

	synchronized void clearWaiveOrdersForPower(Power power) {
		WVEOrder wveOrder = new WVEOrder(power);
		wveOrders.remove(wveOrder);
	}

	synchronized void clearWaiveOrderForPower(Power power) {
		for (int i = 0; i < wveOrders.size(); i++) {
			if (new WVEOrder(power).equals(wveOrders.get(i))) {
				wveOrders.remove(i);
				break;
			}
		}
	}

	synchronized int getWaiveOrdersForPower(Power power) {
		int num = 0;
		for (WVEOrder wveOrder : wveOrders) {
			if (wveOrder.getPower().equals(power)) {
				num++;
			}
		}
		return num;
	}

	synchronized void addWaiveOrder(WVEOrder wveOrder) {
		wveOrders.add(wveOrder);
	}

	synchronized List<Region> getSubmittedOrdersForPower(Power power) {
		List<Region> subOrders = new ArrayList<>();
		Set<Region> keys = submittedOrders.keySet();
		for (Region region : keys) {
			if (submittedOrders.get(region).getPower().equals(power)) {
				subOrders.add(region);
			}
		}
		return subOrders;
	}

	synchronized private void processSuccessfulREMOrder(Order order) {
		if (order instanceof REMOrder) {
			List<Region> currentRegions = game.getPower(order.getPower().getName()).getControlledRegions();
			game.getPower(order.getPower().getName()).resetControl();
			for (Region region : currentRegions) {
				if (!region.equals(order.getLocation())) {
					game.getPower(order.getPower().getName()).addControlledRegion(region);
				}
			}
		}
	}

	synchronized private void processSuccessfulBLDOrder(Order order) {
		if (order instanceof BLDOrder) {
			game.getPower(order.getPower().getName()).addControlledRegion(order.getLocation());
		}
	}

	synchronized private void processSuccessfulDSBOrder(Order order) {
		if (order instanceof DSBOrder) {
			//remove from disloged regions
			HashMap<Region, Dislodgement> dislodgementHashMap = game.getDislodgedRegions();
			Set<Region> keys = dislodgementHashMap.keySet();
			game.resetDislodgedRegions();
			for (Region region : keys) {
				if (!region.equals(order.getLocation())) { // if not the region just moved add it back
					game.addDislodgedRegion(region, dislodgementHashMap.get(region));
				}
			}
		}
	}

	synchronized private void processSuccessfulRTOOrder(Order order) {
		if (order instanceof RTOOrder) {
			//add destination region
			game.getPower(order.getPower().getName()).addControlledRegion(((RTOOrder) order).getDestination());
			if (((RTOOrder) order).getDestination().getProvince().isSC()) { //if its an SC add it to its owned Homes
				game.getPower(order.getPower().getName()).addHome(((RTOOrder) order).getDestination().getProvince());
			}
			//remove from disloged regions
			HashMap<Region, Dislodgement> dislodgementHashMap = game.getDislodgedRegions();
			Set<Region> keys = dislodgementHashMap.keySet();
			game.resetDislodgedRegions();
			for (Region region : keys) {
				if (!region.equals(order.getLocation())) { // if not the region just moved add it back
					game.addDislodgedRegion(region, dislodgementHashMap.get(region));
				}
				List<Province> currentProvinces = order.getPower().getOwnedSCs();
				game.getPower(order.getPower().getName()).resetOwn();
				for (Province province : currentProvinces) {
					if (!province.equals(region.getProvince())) {
						game.getPower(order.getPower().getName()).addOwn(province);
					}
				}
			}
		}
	}

	synchronized private void processSuccessfulMTOOrder(Order o) {
		Province takenOver = ((MTOOrder) o).getDestination().getProvince(); // get the province that is about to be taken over
		for (Power power : game.getPowers()) { //for all powers
			if (!power.equals(o.getPower())) {
				List<Region> powerRegions = power.getControlledRegions();
				game.getPower(power.getName()).resetControl();
				for (Region region : powerRegions) {
					if (!region.getProvince().equals(takenOver)) {
						game.getPower(power.getName()).addControlledRegion(region);
					} else {
						Dislodgement thisDislodgement = new Dislodgement(power, region);
						for (Region thisRegion : region.getAdjacentRegions()) {
							boolean owned = false;
							for (Power thisPower : game.getPowers()) {
								if (thisPower.isControlling(thisRegion)) {
									owned = true;
								}
							}
							if (!owned) {
								thisDislodgement.addRetreateToRegion(thisRegion);
							}
						}
						game.addDislodgedRegion(region, thisDislodgement);
					}
				}

				List<Province> powerProvinces = power.getOwnedSCs();
				game.getPower(power.getName()).resetOwn();
				for (Province province : powerProvinces) {
					if (!province.equals(takenOver)) {
						game.getPower(power.getName()).addOwn(province);
					}
				}
			}
		}
		String power = o.getPower().getName(); //power issuing the order

		List<Region> pRegions = game.getPower(power).getControlledRegions(); //get the power's controlled regions
		game.getPower(power).resetControl(); //reset the power's region list to null
		for (Region region : pRegions) { // for all listed regions
			if (!region.equals(o.getLocation())) {
				game.getPower(power).addControlledRegion(region);
			}
		}
		game.getPower(power).addControlledRegion(((MTOOrder) o).getDestination()); // adding the new region to the power's region list

		if (!game.getPower(power).isOwning(((MTOOrder) o).getDestination().getProvince())) { //if the power does not already own the province
			if (((MTOOrder) o).getDestination().getProvince().isSC()) { // if the destination province is a supply centre
				game.getPower(power).addOwn(((MTOOrder) o).getDestination().getProvince()); //add the SC to the power's SC list
			}
		}

		//if region was added as a previous dislodgement remove it as it has now been moved successfully
		HashMap<Region, Dislodgement> dislodgementHashMap = game.getDislodgedRegions();
		game.resetDislodgedRegions();
		Set<Region> keys = dislodgementHashMap.keySet();
		for (Region region : keys) {
			if (!region.equals(o.getLocation())) {
				game.addDislodgedRegion(region, dislodgementHashMap.get(region));
			}
		}
	}

	synchronized private List<Message> addFailedOrdersToORDList(Order o, List<Message> ORDs, List<Order> ordersForAllPowers) {
		if (o instanceof MTOOrder || o instanceof RTOOrder) {
			boolean RET = false;
			if (game.getDislodgedRegions().get(o.getLocation()) != null) {
				RET = true;
			}
			ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "BNC", RET));
		} else if (o instanceof SUPOrder) {
			boolean RET = false;
			if (game.getDislodgedRegions().get(o.getLocation()) != null) {
				RET = true;
			}
			if (ordersForAllPowers.contains(((SUPOrder) o).getSupportedOrder())) {
				ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "CUT", RET));
			} else {
				ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "NSO", RET));
			}
		} else if (o instanceof SUPMTOOrder) {
			boolean RET = false;
			if (game.getDislodgedRegions().get(o.getLocation()) != null) {
				RET = true;
			}
			if (ordersForAllPowers.contains(((SUPMTOOrder) o).getSupportedOrder())) {
				ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "CUT", RET));
			} else {
				ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "NSO", RET));
			}
		} else {
			boolean RET = false;
			if (game.getDislodgedRegions().get(o.getLocation()) != null) {
				RET = true;
			}
			ORDs.add(ResponseBuilder.ORD(game.getPhase(), game.getYear(), o, "MBV", RET));
		}

		return ORDs;
	}

	synchronized private void addMissingHLDOrders() {
		for (Power power : game.getPowers()) {
			for (Region region : power.getControlledRegions()) {
				if (getOrderForRegion(region) == null) {
					HLDOrder civilDisorder = new HLDOrder(power, region);
					submittedOrders.put(region, civilDisorder);
				}
			}
		}
	}

	synchronized private void addMissingWVEOrders() {
		for (Power power : game.getPowers()) {
			int numSCs = power.getOwnedSCs().size();
			int numRegions = power.getControlledRegions().size();
			int ordersRequired = numSCs - numRegions;
			if (ordersRequired > 0) {
				Set<Region> keys = submittedOrders.keySet();
				for (Region region : keys) {
					Order order = submittedOrders.get(region);
					if (order.getPower() == power && order instanceof BLDOrder) {
						ordersRequired--;
					}
				}
				ordersRequired -= getWaiveOrdersForPower(power);
				if (ordersRequired > 0) {
					for (int i = 0; i < ordersRequired; i++) {
						wveOrders.add(new WVEOrder(power));
					}
				}
			} else if (ordersRequired < 0) {
				List<Region> untouched = new ArrayList<>();
				for (Region region : power.getControlledRegions()) {
					Order order = submittedOrders.get(region);
					if (order != null && order instanceof REMOrder) {
						ordersRequired++;
					} else if (order == null) {
						untouched.add(region);
					}
				}
				if (ordersRequired < 0) {
					for (int i = ordersRequired; i < 0; i++) {
						if (untouched.size() > 0) {
							submittedOrders.put(untouched.get(0), new REMOrder(power, untouched.get(0)));
							untouched.remove(0);
						}
					}
				}
			}
		}
	}

	synchronized private void addMissingDSBOrders() {
		HashMap<Region, Dislodgement> dislodgementHashMap = game.getDislodgedRegions();
		Set<Region> keys = dislodgementHashMap.keySet();
		for (Region region : keys) {
			if (getOrderForRegion(region) == null) {
				DSBOrder dsbOrder = new DSBOrder(region, dislodgementHashMap.get(region).getPower());
				submittedOrders.put(region, dsbOrder);
			}
		}
	}

	synchronized private void updatePlayerStatuses() {
		for (Power power : game.getPowers()) {
			if (power.getControlledRegions().size() == 0) {
				for (ConnectionHandler connectionHandler : playerConnections) {
					if (connectionHandler.getPowerName().equals(power.getName())) {
						connectionHandler.setEliminated(true);
						connectionHandler.setEliminationYear(game.getYear());
					}
					if (power.getOwnedSCs().size() == 0) {
						connectionHandler.setEliminationYear(game.getYear());
					}
				}
			}
		}
	}

	synchronized private void printOrderList(List<Order> ordersForAllPowers, InternalAdjudicator internalAdjudicator) {
		System.out.println(game.getPhase() + " " + game.getYear());
		System.out.println("Num of Orders: " + ordersForAllPowers.size());
		if (ordersForAllPowers.isEmpty()) {
			return;
		}
		for (Order o : ordersForAllPowers) {
			if (!(o instanceof WVEOrder)) {
				System.out.print(o.getPower() + " " + o.getLocation().getName());
			} else {
				System.out.println(o.getPower().getName() + " has submitted a WAIVE order. ");
			}
			if (o instanceof MTOOrder) {
				System.out.println(" MTO " + ((MTOOrder) o).getDestination() + internalAdjudicator.getResult(o));
			} else if (o instanceof HLDOrder) {
				System.out.println(" is HOLDING " + internalAdjudicator.getResult(o));
			} else if (o instanceof SUPOrder) {
				System.out.println(" is Supporting Unit in " + ((SUPOrder) o).getSupportedRegion()
						+ internalAdjudicator.getResult(o));
			} else if (o instanceof SUPMTOOrder) {
				System.out.println(" is Supporting MTO by " + ((SUPMTOOrder) o).getSupportedRegion()
						+ " to " + ((SUPMTOOrder) o).getSupportedOrder().getDestination() + internalAdjudicator.getResult(o));
			} else if (o instanceof RTOOrder) {
				System.out.println(" RTO " + ((RTOOrder) o).getDestination().getName() + internalAdjudicator.getResult(o));
			} else if (o instanceof DSBOrder) {
				System.out.println(" DSB " + internalAdjudicator.getResult(o));
			} else if (o instanceof BLDOrder) {
				System.out.println(" BLD ");
			} else if (o instanceof REMOrder) {
				System.out.println(" REM ");
			} else {
				System.out.println();
			}
		}
	}

	/**
	 * Disconnect the GameManager from all its clients.
	 */
	void closeAllConnections() {

		for (ConnectionHandler observerConnection : observerConnections) {
			observerConnection.sendMessage(ResponseBuilder.OFF(), serverLogger);
			observerConnection.closeConnection();
		}
		observerConnections.clear();

		for (ConnectionHandler playerConnection : playerConnections) {
			playerConnection.sendMessage(ResponseBuilder.OFF(), serverLogger);
			playerConnection.closeConnection();
		}
		playerConnections.clear();
	}

	synchronized public void addCivilDisorder(Power power) {
		if (power == null) {
			return;
		}
		if (power.getControlledRegions() == null) {
			return;
		}
		if (gameIsFinished() || drawUnanimouslyAccepted() || power.getControlledRegions().size() == 0) {
			return;
		}
		if (!civilDisorder.contains(power)) {
			civilDisorder.add(power);
			sendToAllConnectionHandlers(ResponseBuilder.CCD(power));
			if (gameInfo.isDSD()) {
				int diff = (int) ((deadLine - System.currentTimeMillis()) / 1000);
				sendToAllConnectionHandlers(ResponseBuilder.NOT(ResponseBuilder.TME(diff).messageContent));
			}
		}
	}

	synchronized public void checkForCivilDisorders() {
		if (gameIsFinished() || drawUnanimouslyAccepted() || game.getPhase() == Phase.WIN) {
			return;
		}
		if (getExpectedOrders() == 0) {
			return;
		}
		for (Power power : game.getPowers()) {
			if (power.getControlledRegions().size() == 0) {
				continue;
			}
			boolean submitted = false;
			switch (game.getPhase()) {
				case WIN:
					int diff = power.getOwnedSCs().size() - power.getControlledRegions().size();
					if (diff != 0) {
						if (!powerHasSubmittedOrders(power)) {
							addCivilDisorder(power);
						}
					}
					break;
				case SPR:
				case FAL:
					if (!powerHasSubmittedOrders(power)) {
						addCivilDisorder(power);
					}
					break;
				case AUT:
				case SUM:
					boolean hasDislodged = false;
					if (game.getDislodgedRegions(power).size() > 0) {
						if (!powerHasSubmittedOrders(power)) {
							addCivilDisorder(power);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	synchronized public boolean powerHasSubmittedOrders(Power power) {
		if (game.getPhase() == Phase.WIN) {
			if (wveOrders.contains(power)) {
				return true;
			}
		}
		Set<Region> keys = submittedOrders.keySet();
		for (Region region : keys) {
			Order order = submittedOrders.get(region);
			if (order.getPower().equals(power)) {
				return true;
			}
		}
		return false;
	}

	synchronized public void removeCivilDisorder(Power power) {
		if (gameIsFinished() || drawUnanimouslyAccepted()) {
			return;
		}
		if (civilDisorder.remove(power)) {
			sendToAllConnectionHandlers(ResponseBuilder.NOT(ResponseBuilder.CCD(power).messageContent));
			if (gameInfo.isDSD()) {
				int diff = (int) ((deadLine - System.currentTimeMillis()) / 1000);
				sendToAllConnectionHandlers(ResponseBuilder.TME(diff));
			}
		}
	}

	synchronized public boolean isInCivilDisorder(Power power) {
		if (civilDisorder.contains(power)) {
			return true;
		}
		return false;
	}

	synchronized public boolean getAOA() {
		return gameInfo.isAOA();
	}

	synchronized public void setAcceptingGOF(ConnectionHandler handler, boolean value) {
		acceptingGOF.put(handler, value);
	}

	synchronized private boolean isAcceptingGOF() {
		if (acceptingGOF.containsValue(false)) {
			return false;
		}
		return true;
	}

	synchronized private void resetAcceptingGOF() {
		Set<ConnectionHandler> keys = acceptingGOF.keySet();
		acceptingGOF.clear();
		for (ConnectionHandler connectionHandler : keys) {
			acceptingGOF.put(connectionHandler, true);
		}
	}

	synchronized private void resetAcceptingDraw() {
		acceptingDraw.clear();
	}

	synchronized public void setPowerAcceptingDraw(ConnectionHandler connectionHandler, ProposedDraw key) {
		List<ConnectionHandler> acceptances = acceptingDraw.get(key.toString());
		if (acceptances == null) {
			acceptances = new ArrayList<>();
		}
		acceptances.add(connectionHandler);
		acceptingDraw.put(key.toString(), acceptances);
	}

	synchronized public boolean drawUnanimouslyAccepted() {
		if (acceptingDraw.size() <= 0) {
			return false;
		}
		int numPlayers = getAlivePlayers().size();
		for (String key : acceptingDraw.keySet()) {
			List<ConnectionHandler> acceptances = acceptingDraw.get(key);
			if (acceptances.size() == numPlayers) {
				return true;
			}
		}
		return false;
	}

	synchronized public void sendToAllConnectionHandlers(Message message) {
		for (ConnectionHandler connectionHandler : observerConnections) {
			if (connectionHandler.isRecieving()) {
				connectionHandler.sendMessage(message, serverLogger);
			}
		}
		for (ConnectionHandler connectionHandler : playerConnections) {
			if (connectionHandler.isRecieving()) {
				connectionHandler.sendMessage(message, serverLogger);
			}
		}
	}

	synchronized public void sendToPowers(List<String> powers, Message message) {
		for (ConnectionHandler connectionHandler : playerConnections) {
			if (connectionHandler.isRecieving() && powers.contains(connectionHandler.getPowerName())) {
				connectionHandler.sendMessage(message, serverLogger);
			}
		}
	}

	synchronized public ConnectionHandler removePowerFromPlayerConnections(Power power) {
		ConnectionHandler oldHandler = null;
		for (ConnectionHandler connectionHandler : playerConnections) {
			if (connectionHandler.getPowerName().equals(power.getName())) {
				oldHandler = connectionHandler;
			}
		}
		if (oldHandler != null) {
			removePlayerHandler(oldHandler);
		}
		return oldHandler;
	}

	synchronized public HLOLogin getHLOLoginForPower(Power power) {
		return hloHashMap.get(power);
	}

	synchronized public List<Message> getTurnHistory(String turn) {
		return orderHistory.get(turn);
	}

	synchronized public List<Message> addTurnHistory(String turn, List<Message> messageList) {
		return orderHistory.put(turn, messageList);
	}

	synchronized public boolean orderHistoryHasKey(String key) {
		return orderHistory.containsKey(key);
	}

	synchronized public Set<String> orderHistoryKeys() {
		return orderHistory.keySet();
	}

	synchronized public int getExpectedOrders() {
		int sum = 0;
		if (game.getPhase() == Phase.WIN) {
			for (Power power : game.getPowers()) {
				sum += Math.abs(power.getOwnedSCs().size() - power.getControlledRegions().size());
			}
		} else if (game.getPhase() == Phase.SUM || game.getPhase() == Phase.AUT) {
			return game.getDislodgedRegions().size();
		} else {
			for (Power power : game.getPowers()) {
				sum += power.getControlledRegions().size();
			}
		}
		return sum;
	}

	synchronized public boolean isResolvingRound() {
		return resolvingRound;
	}

	synchronized public void setGameStarted(boolean gameStarted) {
		this.gameStarted = gameStarted;
	}

	synchronized public boolean isGameStarted() {
		return gameStarted;
	}

	synchronized public boolean isPowerInCCD(String powerName) {
		Power power = game.getPower(powerName);
		if (civilDisorder.contains(power)) {
			return true;
		}
		return false;
	}

	synchronized public boolean isPowerDead(String powerName) {
		for (ConnectionHandler connectionHandler : playerConnections) {
			if (connectionHandler.getPowerName().equals(powerName)) {
				if (connectionHandler.isEliminated()) {
					return true;
				}
				return false;
			}
		}
		return false;
	}

	synchronized public List<Power> getAlivePlayers() {
		List<Power> alivePowers = new ArrayList<>();
		for (ConnectionHandler connectionHandler : playerConnections) {
			if (!connectionHandler.isEliminated()) {
				alivePowers.add(game.getPower(connectionHandler.getPowerName()));
			}
		}
		return alivePowers;
	}

	synchronized public boolean isEgalitarian() {
		return gameInfo.isEgalitarian();
	}

	synchronized public boolean allPlayersReady() {
		for (ConnectionHandler connectionHandler : playerConnections) {
			if (!connectionHandler.isReady()) {
				return false;
			}
		}
		return true;
	}

	synchronized public void startGame() {
		for (ConnectionHandler player : playerConnections) {
			Power power = game.getPower(player.getPowerName());
			HLOLogin hloLogin = hloHashMap.get(power);
			player.sendMessage(ResponseBuilder.HLO(power.getName(), hloLogin.getPasscode(), gameInfo), serverLogger);
			acceptingDraw.clear();
			acceptingGOF.put(player, true);
		}

		for (ConnectionHandler player : observerConnections) {
			if (player.isRecieving()) {
				player.sendMessage(ResponseBuilder.HLO("UNO", 0, gameInfo), serverLogger);
			}
		}

		for (ConnectionHandler connectionHandler : playerConnections) {
			System.out.println(connectionHandler.getClientName() + " is: " + connectionHandler.getPowerName());
		}

		setGameStarted(true);
		sendNOWandSCO(true);
		setNextDeadLine();
	}
}