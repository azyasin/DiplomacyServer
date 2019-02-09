package bandana.gameServer;

import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import es.csic.iiia.fabregues.dip.board.*;
import es.csic.iiia.fabregues.dip.orders.*;

import java.util.*;
import java.util.regex.*;

public class MessageHandler {
	static void handleMessage(Message message, GameManager manager, ConnectionHandler handler, GameInfo gameInfo) {
		ValidationValue errorLocation = MessageValidator.isValid(message, gameInfo.getLevel());
		manager.serverLogger.LogMessage(handler.getClientName() + " -> Server: " + message.toString());
		if (handler.getPowerName() != null) {
			//System.out.println(message.toString() + " " + message.toByteString() + " " + errorLocation.isValid() + " " + errorLocation.getLocation() + " " + message.messageContent.length);
		}
		if (!manager.gameIsFinished() && manager.isInCivilDisorder(manager.game.getPower(handler.getPowerName()))) {
			manager.removeCivilDisorder(manager.game.getPower(handler.getPowerName()));
		}
		// if player was in CCD removes that status
		// Message is valid process
		if (errorLocation.isValid()) {
			String initialToken = TokenTranslator.toString(message.messageContent[0], message.messageContent[1]);
			switch (initialToken) {
				case "NME":
					if (manager.getConnectedPlayerNames().size() < 7 && !manager.isGameStarted()) {
						manager.removeConnectionHandler(handler);
						handler.setClientName(getPlayerNameFromMessage(message, manager.getConnectedPlayerNames().size()));
						handler.setClientVersion(getPlayerVersionFromMessage(message));
						manager.addPlayerHandler(handler);
						try {
							handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
							handler.sendMessage(ResponseBuilder.MAP("standard"), manager.serverLogger);
							handler.setIsPlayer(true);
							handler.isRecieving(true);
						} catch (Exception e) {
							manager.serverLogger.LogError(e.getMessage());
						}
					} else {
						try {
							handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
							handler.isRecieving(false);
						} catch (Exception e) {
							manager.serverLogger.LogError(e.getMessage());
						}
					}
					break;
				case "OBS":
					//Send YES(OBS)
					handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
					handler.setClientName("OBS");
					handler.sendMessage(ResponseBuilder.MAP("standard"), manager.serverLogger);
					handler.isRecieving(true);
					break;
				case "IAM":
					//Reconnection by player
					String powerName = TokenTranslator.toString(message.messageContent[4], message.messageContent[5]);
					HLOLogin hloLogin = manager.getHLOLoginForPower(manager.game.getPower(powerName));
					System.out.println(TokenTranslator.toString(message.messageContent[10], message.messageContent[11]) +
							" " + powerName + " " + hloLogin.getPasscode());
					int passcode = Integer.parseInt(TokenTranslator.toString(message.messageContent[10], message.messageContent[11]));
					if (passcode == hloLogin.getPasscode()) {
						handler.setClientName(hloLogin.getClientName());
						handler.setPowerName(powerName);
						handler.setClientVersion(hloLogin.getClientVersion());
						manager.addPlayerHandler(handler);
						handler.setIsPlayer(true);
						handler.isRecieving(true);
						handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
					}
					else {
						System.out.println("Passcodes do not match");
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					break;
				case "MAP":
					//respond with MAP(string) where string is the map name
					handler.sendMessage(ResponseBuilder.MAP("standard"), manager.serverLogger);
					break;
				case "MDF":
					//respond with MDF
					handler.sendMessage(ResponseBuilder.MDF(manager.game), manager.serverLogger);
					break;
				case "YES":
					//acknowledged contents - process on contents
					if (message.toString().contains("MAP")) {
						handler.setReady(true);
					}
					break;
				case "REJ":
					//rejected contents - Log rejection as error
					try {
						throw new RuntimeException(handler.getClientName() + ": " + message.toString());
					} catch (Exception e) {
						manager.serverLogger.LogError(e.getMessage());
					}
					break;
				case "NOW":
					if (manager.isGameStarted()) {
						handler.sendMessage(ResponseBuilder.NOW(manager.game.getPowers(), manager.game.getYear(),
								manager.game.getPhase(), manager.game), manager.serverLogger);
					}
					else {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					break;
				case "SCO":
					if (manager.isGameStarted()) {
						handler.sendMessage(ResponseBuilder.SCO(manager.game.getPowers(),
								manager.game.getProvinces()), manager.serverLogger);
					}
					else {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					break;
				case "HST":
					if (message.messageContent.length == 10 && manager.isGameStarted()) {
						String season = TokenTranslator.toString(message.messageContent[4], message.messageContent[5]);
						Phase phase = Phase.SPR;
						switch (season) {
							case "SPR":
								phase = Phase.SPR;
								break;
							case "SUM":
								phase = Phase.SUM;
								break;
							case "FAL":
								phase = Phase.FAL;
								break;
							case "AUT":
								phase = Phase.AUT;
								break;
							case "WIN":
								phase = Phase.WIN;
								break;
						}
						int year = Integer.parseInt(TokenTranslator.toString(message.messageContent[6], message.messageContent[7]));
						Turn turn = new Turn(year, phase);
						List<Message> messages = manager.getTurnHistory(turn.toString());
						for(Message message1: messages) {
							handler.sendMessage(message1, manager.serverLogger);
						}
					}
					else {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					break;
				case "TME":
					if (gameInfo.getBuildTimeLimit() > 0 || gameInfo.getMoveTimeLimit() > 0
							|| gameInfo.getRetreatTimeLimit() > 0) {
						handler.setTme(true);
						if (message.messageContent.length > 2) {
							String seconds;
							seconds = TokenTranslator.toString(message.messageContent[4], message.messageContent[5]);
							long deadline = manager.getDeadLine();
							long currentTime = System.currentTimeMillis();
							long diff = deadline - currentTime;
							if ((long)Integer.parseInt(seconds)*1000 != diff) {
								handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
								handler.sendMessage(ResponseBuilder.TME((int)diff/1000), manager.serverLogger);
							}
							else {
								handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
							}
						}
						else {
							long deadline = manager.getDeadLine();
							long currentTime = System.currentTimeMillis();
							long diff = deadline - currentTime;
							handler.sendMessage(ResponseBuilder.TME((int)diff/1000), manager.serverLogger);
						}
					}
					else {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					break;
				case "ADM":
					manager.sendToAllConnectionHandlers(message);
					break;
				case "PRN":
					try {
						throw new RuntimeException(message.toString());
					} catch (RuntimeException e) {
						manager.serverLogger.LogError(e.getMessage());
					}
					break;
				case "HUH":
					try {
						throw new RuntimeException(message.toString());
					} catch (RuntimeException e) {
						manager.serverLogger.LogError(e.getMessage());
					}
					break;
				case "HLO":
					if (!manager.isGameStarted()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					else if (handler.isPlayer()) {
						HLOLogin hloLogin1 = manager.getHLOLoginForPower(manager.game.getPower(handler.getPowerName()));
						handler.sendMessage(ResponseBuilder.HLO(hloLogin1.getHLOPower().getName(), hloLogin1.getPasscode(),
								gameInfo), manager.serverLogger);
					}
					else {
						handler.sendMessage(ResponseBuilder.HLO("UNO", 0, gameInfo), manager.serverLogger);
					}
					break;
				case "SUB":
					if (!manager.isGameStarted()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					if (manager.isResolvingRound()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					if (handler.isEliminated() ) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					List<Order> orderList = convertToSUBOrders(message, manager.game, handler);
					for (Order o: orderList) {
						if (o != null) {
							Message oResponse = checkAndGetRespondToOrder(o, handler, manager);
							if (oResponse.toString().contains("(MBV)") || manager.getAOA()) {
								if (o instanceof WVEOrder) {
									manager.addWaiveOrder((WVEOrder) o);
								}
								else {
									manager.addSubmittedOrder(o);
								}
							}
							handler.sendMessage(oResponse, manager.serverLogger);
						}
					}
					missingOrders(manager, handler);
					break;
				case "NOT":
					String rejected = TokenTranslator.toString(message.messageContent[4], message.messageContent[5]);
					switch (rejected) {
						case "SUB":
							if (!manager.isResolvingRound() && !handler.isEliminated() && manager.isGameStarted()) {
								Phase phase = manager.game.getPhase();
								Power power = manager.game.getPower(handler.getPowerName());
								if (message.messageContent.length == 8) {
									for (Region region : power.getControlledRegions()) {
										manager.removeSubmittedOrder(region);
									}
									if (phase == Phase.WIN) {
										manager.clearWaiveOrdersForPower(power);
									}
								} else {
									byte[] newMsg = new byte[message.messageContent.length - 6];
									System.arraycopy(message.messageContent, 4, newMsg, 0, message.messageContent.length - 2 - 4);
									orderList = convertToSUBOrders(new Message((byte) 2, newMsg), manager.game, handler);
									for (Order order : orderList) {
										if (order instanceof WVEOrder) {
											manager.clearWaiveOrderForPower(power);
										} else {
											manager.removeSubmittedOrder(order.getLocation());
										}
									}
								}
								handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
							}
							else {
								handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
							}
							break;
						case "GOF":
							if (manager.isGameStarted() || manager.getDeadLine() == 0 || handler.isEliminated()) {
								handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
							}
							else {
								manager.setAcceptingGOF(handler, false);
								handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
							}
							break;
						case "TME":
							if (handler.isTme()) {
								handler.setTme(false);
								handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
							}
							else {
								handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
							}
							break;
						case "DRW":
							break;

					}
					break;
				case "MIS":
					if (manager.isGameStarted() || manager.isResolvingRound()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					else {
						Power power = manager.game.getPower(handler.getPowerName());
						switch (manager.game.getPhase()) {
							case WIN:
								int num_builds = power.getOwnedSCs().size() - power.getControlledRegions().size();
								num_builds -= manager.getWaiveOrdersForPower(power);
								num_builds -= manager.getSubmittedOrdersForPower(power).size();
								handler.sendMessage(ResponseBuilder.MIS(num_builds * -1), manager.serverLogger);
								break;
							case SPR:
							case FAL:
								List<Region> missing = new ArrayList<>();
								List<Region> submitted = manager.getSubmittedOrdersForPower(power);
								for (Region region : power.getControlledRegions()) {
									if (!submitted.contains(region)) {
										missing.add(region);
									}
								}
								handler.sendMessage(ResponseBuilder.MIS(missing, power), manager.serverLogger);
								break;
							case AUT:
							case SUM:
								HashMap<Region, Dislodgement> missingDislodgementHashMap = new HashMap<>();
								HashMap<Region, Dislodgement> dislodgementHashMap = manager.game.getDislodgedRegions();
								Set<Region> keys = dislodgementHashMap.keySet();
								for (Region region : keys) {
									if (manager.getOrderForRegion(region) == null) {
										missingDislodgementHashMap.put(region, dislodgementHashMap.get(region));
									}
								}
								handler.sendMessage(ResponseBuilder.MIS(missingDislodgementHashMap, power), manager.serverLogger);
								break;
							default:
								break;
						}
					}
					break;
				case "GOF":
					if (manager.isGameStarted()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
					}
					else {
						manager.setAcceptingGOF(handler, true);
						handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
					}
					break;
				case "ORD":
					if (!manager.isGameStarted()) {
						//System.out.println("Game is not started");
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					Turn turn = new Turn(manager.game.getYear(), manager.game.getPhase());
					//System.out.println(turn.getYear() + turn.getPhase().name());
					turn = getPreviousTurn(turn);
					//System.out.println(turn.getYear() + turn.getPhase().name());
					List<Message> turnHistory = null;
					while (true) {
						if (turn.getYear() < 1901) {
							handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
							return;
						}
						if (manager.getTurnHistory(turn.toString()) == null || manager.getTurnHistory(turn.toString()).size() <= 2) {
							turn = getPreviousTurn(turn);
						}
						if (turn.getPhase() != Phase.WIN) {
							turnHistory = manager.getTurnHistory(turn.toString());
							for (Message message1: turnHistory) {
								if (message1.toString().contains("ORD")) {
									handler.sendMessage(message1, manager.serverLogger);
								}
							}
							break;
						}
					}
				case "DRW":
					if (!manager.isGameStarted() || manager.gameIsFinished()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					if (message.messageContent.length > 2) {
						List<String> powersInDraw = new ArrayList<>();
						for (int i = 4; i < message.messageContent.length - 2; i+=2) {
							powersInDraw.add(TokenTranslator.toString(message.messageContent[i], message.messageContent[i+1]));
						}
						ProposedDraw proposedDraw = new ProposedDraw(powersInDraw);
						manager.setPowerAcceptingDraw(handler, proposedDraw);
						handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
					}
					else {
						List<String>powersInDraw = new ArrayList<>();
						for (Power power1: manager.getAlivePlayers()) {
							powersInDraw.add(power1.getName());
						}
						ProposedDraw proposedDraw = new ProposedDraw(powersInDraw);
						manager.setPowerAcceptingDraw(handler, proposedDraw);
						handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
					}
					break;
				case "SND":
					if (gameInfo.getLevel() <= 0 || !manager.isGameStarted() || manager.gameIsFinished()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					if (System.currentTimeMillis() >= manager.getPressDeadLine()) {
						handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
						return;
					}
					int pos = 0;
					switch (message.messageContent[4]) {
						case (byte)0x41: //power
							pos = 4;
							break;
						case (byte)0x47: //turn specified
							String phase = TokenTranslator.toString(message.messageContent[4], message.messageContent[5]);
							String year = TokenTranslator.toString(message.messageContent[6], message.messageContent[7]);
							if (!manager.game.getPhase().name().equals(phase) || manager.game.getYear() != Integer.parseInt(year)) {
								handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
								return;
							}
							pos = 12;
							break;
						default:
							handler.sendMessage(ResponseBuilder.REJ(message.messageContent), manager.serverLogger);
							break;
					}
					List<String> recipients = new ArrayList<>();
					String thisToken = TokenTranslator.toString(message.messageContent[pos], message.messageContent[pos + 1]);
					while (!thisToken.equals(")") && pos < message.messageContent.length) {
						recipients.add(thisToken);
						pos += 2;
						thisToken = TokenTranslator.toString(message.messageContent[pos], message.messageContent[pos + 1]);
					}
					for (String name: recipients) {
						if (manager.isPowerInCCD(name)) {
							handler.sendMessage(ResponseBuilder.CCD(manager.game.getPower(name)), manager.serverLogger);
							return;
						}
						if (manager.isPowerDead(name)) {
							handler.sendMessage(ResponseBuilder.OUT(manager.game.getPower(name)), manager.serverLogger);
							return;
						}
					}
					pos+=4;
					byte[] content = new byte[message.messageContent.length - (pos + 2)];
					for (int i = 0; i < content.length; i++) {
						content[i] = message.messageContent[i + pos];
					}
					manager.sendToPowers(recipients, ResponseBuilder.FRM(handler.getPowerName(), recipients, content));
					handler.sendMessage(ResponseBuilder.YES(message.messageContent), manager.serverLogger);
					break;
				default:
					break;
			}
		} else {
			if (!errorLocation.isValid() && errorLocation.getLocation() == -2) {
				handler.sendMessage(ResponseBuilder.PRN(message.messageContent), manager.serverLogger);
			}
			else {
				handler.sendMessage(ResponseBuilder.HUH(message.messageContent, errorLocation.getLocation()), manager.serverLogger);
			}
		}
	}

	private static void missingOrders(GameManager manager, ConnectionHandler connectionHandler){
		Phase phase = manager.game.getPhase();
		if (phase == Phase.SPR || phase == Phase.FAL) {
			List<Region> missingRegions = new ArrayList<>();
			for (Region region: manager.game.getPower(connectionHandler.getPowerName()).getControlledRegions()) {
				if (manager.getOrderForRegion(region) == null) {
					missingRegions.add(region);
				}
			}
			if (missingRegions.size() > 0) {
				connectionHandler.sendMessage(ResponseBuilder.MIS(missingRegions,
						manager.game.getPower(connectionHandler.getPowerName())), manager.serverLogger);
			}
		}
		if (phase == Phase.SUM || phase == Phase.AUT) {
			List<Region> dislodgedRegions;
			dislodgedRegions = manager.game.getDislodgedRegions(manager.game.getPower(connectionHandler.getPowerName()));
			HashMap<Region, Dislodgement> dislodgementRegionHashMap = manager.game.getDislodgedRegions();
			HashMap<Region, Dislodgement> missingdislodgementHashMap = new HashMap<>();
			for (Region region: dislodgedRegions) {
				if (manager.getOrderForRegion(region) == null) {
					missingdislodgementHashMap.put(region, dislodgementRegionHashMap.get(region));
				}
			}
			if (missingdislodgementHashMap.size() > 0) {
				connectionHandler.sendMessage(ResponseBuilder.MIS(missingdislodgementHashMap,
						manager.game.getPower(connectionHandler.getPowerName())), manager.serverLogger);
			}
		}
		if (phase == Phase.WIN) {
			Power power = manager.game.getPower(connectionHandler.getPowerName());
			int maxAllowed = power.getOwnedSCs().size();
			int numRegions = power.getControlledRegions().size();
			int diff = maxAllowed - numRegions;
			if (diff > 0) {
				HashMap<Region, Order> orderHashMap = manager.getSubmittedOrders();
				Set<Region> keys = orderHashMap.keySet();
				for (Region region: keys) {
					Order order = orderHashMap.get(region);
					if (order.getPower().equals(power) && (order instanceof BLDOrder)) {
						diff--;
					}
				}
				diff -= manager.getWaiveOrdersForPower(power);
				if (diff != 0) {
					connectionHandler.sendMessage(ResponseBuilder.MIS(diff), manager.serverLogger);
				}
			}
			else if (diff < 0) {
				HashMap<Region, Order> orderHashMap = manager.getSubmittedOrders();
				Set<Region> keys = orderHashMap.keySet();
				for (Region region: keys) {
					Order order = orderHashMap.get(region);
					if (order.getPower().equals(power) && (order instanceof REMOrder)) {
						diff++;
					}
				}
				if (diff != 0) {
					connectionHandler.sendMessage(ResponseBuilder.MIS(diff), manager.serverLogger);
				}
			}
		}
	}

	private static String getPlayerNameFromMessage(Message message, int number) {
		String[] msgSegments = message.toString().split("[()]");
		return msgSegments[1];
	}

	private static String getPlayerVersionFromMessage(Message message) {
		String[] msgSegments = message.toString().split("[()]");
		return msgSegments[3];
	}

	private static List<Order> convertToSUBOrders(Message message, Game game, ConnectionHandler connectionHandler) {
		String submitted = message.toString();
		List<Order> orders = new ArrayList<>();
		int openingBrackets = 0;
		int closingBrackets = 0;
		int position = 3;
		String currentOrder = "";
		while (position < submitted.length()){
			if (submitted.charAt(position) == '(') {
				openingBrackets++;
			}
			else if (submitted.charAt(position) == ')') {
				closingBrackets++;
			}
			currentOrder += submitted.charAt(position);
			if (openingBrackets == closingBrackets) {
				Order tempOrder = convertToOrder(currentOrder, game, connectionHandler);
				if (tempOrder != null) {
					orders.add(tempOrder);
				}
				openingBrackets = 0;
				closingBrackets = 0;
				currentOrder = "";
			}
			position++;
		}
		return orders;
	}

	private static Order convertToOrder(String orderString, Game game, ConnectionHandler connectionHandler){
		Order order = null;
		List<String> orderParts = new ArrayList<>();
		// String to be scanned to find the pattern.
		String pattern = "([A-Z]{3})";
		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);
		// Now create matcher object.
		Matcher m = r.matcher(orderString);
		while(m.find()) {
			orderParts.add(m.group(0));
		}
		order = createOrderType(orderParts, game, connectionHandler);
		return order;
	}

	static Order createOrderType(List<String> parts, Game game, ConnectionHandler connectionHandler){
		Order order = null;
		HashMap<String, String> orderTypes = Constants.dictionary.getCategory("43");
		int typePos = 3;
		Power power = game.getPower(parts.get(0));
		if (power == null) {
			//System.out.println("Power is null!");
		}
		if (parts.size() < 3) {
			return new WVEOrder(power);
		}
		//List<Region>
		Region region = null;
		if (orderTypes.containsValue(parts.get(3))) {
			typePos = 3;
		}
		else {
			typePos = 4;
		}
		if (typePos == 3){
			region = game.getRegion(parts.get(2) + parts.get(1));
			if (region == null) {
				region = new Region(parts.get(2) + parts.get(1));
				region.setProvince(game.getProvince(parts.get(2)));
			}
		}
		else {
			region = game.getRegion(parts.get(2) + parts.get(3));
			if (region == null) {
				region = new Region(parts.get(2) + parts.get(3));
				region.setProvince(game.getProvince(parts.get(2)));
			}
		}

		switch (parts.get(typePos)) {
			case "HLD":
				order = HLDOrder(power, region);
				break;
			case "MTO":
				order = MTOOrder(parts, typePos, game, power, region);
				break;
			case "SUP":
				int counter = typePos + 1;
				boolean SUPMTO = false;
				while (counter < parts.size() && !SUPMTO) {
					if(parts.get(counter).compareTo("MTO") == 0) {
						SUPMTO = true;
					}
					else {
						counter++;
					}
				}
				if (SUPMTO){
					order = SUPMTOOrder(parts, typePos, game, power, region);
				}
				else {
					order = SUPOrder(parts, typePos, game, power, region);
				}
				break;
			case "CVY":
				//CVY does not exist in this
				break;
			case "CTO":
				//CTO does not exist
				break;
			case "RTO":
				order = RTOOrder(parts, typePos, game, power, region);
				break;
			case "DSB":
				order = DSBOrder(power, region);
				break;
			case "BLD":
				order = BLDOrder(power, region);
				break;
			case "REM":
				order = REMOrder(power, region);
				break;
			default:
				break;
		}

		return order;
	}

	static private HLDOrder HLDOrder(Power power, Region region){
		return new HLDOrder(power, region);
	}

	static private MTOOrder MTOOrder(List<String> parts, int typePos, Game game, Power power, Region region){
		Region dest = null;
		String destName = "";
		String province = "";
		if (region == null) {
			//System.out.println("Region is null");
			return null;
		}
		if(parts.size() - typePos == 2) {
			destName = "";
			if (region.getName().substring(3).compareTo("AMY") == 0 || region.getName().substring(3).compareTo("FLT") == 0) {
				destName = parts.get(parts.size() - 1) + region.getName().substring(3);
				province = parts.get(parts.size() - 1);
			}
			else {
				destName = parts.get(parts.size() - 1) + "FLT";
				province = parts.get(parts.size() - 1);
			}
			dest = game.getRegion(destName);

		}
		else {
			destName = parts.get(parts.size() - 2) + parts.get(parts.size() - 1);
			province = parts.get(parts.size() - 2);
			dest = game.getRegion(destName);
		}
		if (dest == null) {
			dest = new Region(destName);
			dest.setProvince(game.getProvince(province));
		}
		return new MTOOrder(power, region, dest);
	}

	static private SUPMTOOrder SUPMTOOrder(List<String> parts, int typePos, Game game, Power power, Region region){
		int supIndex = parts.indexOf("SUP");
		Power supportedPower = game.getPower(parts.get(supIndex + 1));
		String supportUnitType = parts.get(supIndex + 2);
		String supportedLocationProvince = parts.get(supIndex + 3);
		String supportedLocationCoast = "";
		String supportedDestinationProvince = "";
		String supportedDestinationCoast = "";
		if (parts.get(supIndex + 4).equals("MTO")) {
			supportedDestinationProvince = parts.get(supIndex + 5);
			if (parts.size() > supIndex + 6) {
				supportedDestinationCoast = parts.get(supIndex + 6);
			}
		}
		else {
			supportedLocationCoast = parts.get(supIndex + 4);
			supportedDestinationProvince = parts.get(supIndex + 5);
			if (parts.size() > supIndex + 6) {
				supportedDestinationCoast = parts.get(supIndex + 6);
			}
		}
		String supportedLocationName = supportedLocationProvince;
		if (supportedLocationCoast.length() > 0) {
			supportedLocationName += supportedLocationCoast;
		}
		else {
			supportedLocationName += supportUnitType;
		}
		String supportedDestinationName = supportedDestinationProvince;
		if (supportedDestinationCoast.length() > 0) {
			supportedDestinationName += supportedDestinationCoast;
		}
		else {
			supportedDestinationName += supportUnitType;
		}
		Region supportedLocation = game.getRegion(supportedLocationName);
		if (supportedLocation == null) {
			supportedLocation = new Region(supportedLocationName);
			region.setProvince(game.getProvince(supportedLocationProvince));
			if (region.getProvince() == null) {
				region.setProvince(new Province(supportedLocationProvince));
			}
		}
		Region supportedDestination = game.getRegion(supportedDestinationName);
		if (supportedDestination == null) {
			supportedDestination = new Region(supportedDestinationName);
			supportedDestination.setProvince(game.getProvince(supportedDestinationProvince));
			if (supportedDestination.getProvince() == null) {
				supportedDestination.setProvince(new Province(supportedDestinationProvince));
			}
		}
		MTOOrder supportedOrder = new MTOOrder(supportedPower, supportedLocation, supportedDestination);
		return new SUPMTOOrder(power, region, supportedOrder);
	}

	static private SUPOrder SUPOrder(List<String> parts, int typePos, Game game, Power power, Region region){
		//SUP pow unit prov (coast) (MTO province (coast))
		HLDOrder supportedOrder = null;
		Power supportedPower = game.getPower(parts.get(typePos + 1));
		String supportedUnitType = parts.get(typePos + 2);
		Province supportedProvince = game.getProvince(parts.get(typePos + 3));
		String supportedCoast = "";
		if (typePos + 4 < parts.size()) {
			supportedCoast = parts.get(typePos + 4);
		}
		String supportedRegionName = supportedProvince.getName();
		if (supportedCoast.length() > 0) {
			supportedRegionName += supportedCoast;
		}
		else {
			supportedRegionName += supportedUnitType;
		}
		Region supportedRegion = game.getRegion(supportedRegionName);
		if (supportedRegion == null) {
			supportedRegion = new Region(supportedRegionName);
			supportedRegion.setProvince(supportedProvince);
		}
		supportedOrder = new HLDOrder(supportedPower, supportedRegion);
		return new SUPOrder(power, region, supportedOrder);
	}

	static private RTOOrder RTOOrder(List<String> parts, int typePos, Game game, Power power, Region region){
		Region dest;
		String destName = "";
		Province province = null;
		String unitType = "";
		for(int i = 1; i < parts.size(); i++) {
			String thisPart = parts.get(i);
			if (thisPart.equals("AMY") || thisPart.equals("FLT")) {
				unitType = thisPart;
			}
			if (i == (typePos + 1)) {
				province = game.getProvince(thisPart);
				if(province == null) {
					province = new Province(thisPart);
				}
			}
			if (i == (typePos + 2)) {
				unitType = thisPart;
			}
		}
		dest = game.getRegion(province.getName()+unitType);
		if (dest == null) {
			dest = new Region(province.getName()+unitType);
		}
		return new RTOOrder(region, power, dest);
	}

	static private DSBOrder DSBOrder(Power power, Region region){
		return new DSBOrder(region, power);
	}

	static private BLDOrder BLDOrder(Power power, Region region){
		return new BLDOrder(power, region);
	}

	static private REMOrder REMOrder(Power power, Region region){
		return new REMOrder(power, region);
	}

	static private Message checkAndGetRespondToOrder(Order order, ConnectionHandler connectionHandler, GameManager manager){
		if (noSuchUnit(order, manager.game)) {
			return ResponseBuilder.THX(order, "NSU");
		}
		if (notAdjacent(order, manager.game)) {
			 return ResponseBuilder.THX(order, "FAR");
		}
		if (noSuchProvince(order)) {
			return ResponseBuilder.THX(order, "NSP");
		}
		if (notYourUnit(order, connectionHandler, manager.game)) {
			return ResponseBuilder.THX(order, "NYU");
		}
		if (noRetreatNeeded(order, manager.game)) {
			return ResponseBuilder.THX(order, "NRN");
		}
		if (notValidRetreatSpace(order, manager.game)) {
			return ResponseBuilder.THX(order, "NVR");
		}
		if (notYourSupplyCentre(order, connectionHandler, manager.game)) {
			return ResponseBuilder.THX(order, "YSC");
		}
		if (notEmptySupplyCentre(order, connectionHandler, manager.game)) {
			return ResponseBuilder.THX(order, "ESC");
		}
		if (notHomeSupplyCentre(order, connectionHandler, manager.game)) {
			return ResponseBuilder.THX(order, "HSC");
		}
		if (notSupplyCentre(order, connectionHandler, manager.game)) {
			return ResponseBuilder.THX(order, "NSC");
		}
		if (notAppropriateBuildLocation(order)) {
			return ResponseBuilder.THX(order, "CST");
		}
		if (noMoreRemovalsAllowed(order, manager)) {
			return ResponseBuilder.THX(order, "NMR");
		}
		if (noMoreBuildsAllowed(order, manager)) {
			return ResponseBuilder.THX(order, "NMB");
		}
		if (notRightSeason(order, manager.game)) {
			return ResponseBuilder.THX(order, "NRS");
		}
		if (notAtSea()) {
			//should never occur
			try {
				throw new RuntimeException("Not at sea returned true. Ensure the Diplomacy Server has been adequately expanded to cope with this scenario");
			} catch (Exception e) {
				manager.serverLogger.LogError(e.getMessage());
			}
			return ResponseBuilder.THX(order, "NAS");
		}
		if (noSuchFleet()) {
			//should never occur
			try {
				throw new RuntimeException("No such fleet returned true. Ensure the Diplomacy Server has been adequately expanded to cope with this scenario");
			} catch (Exception e) {
				manager.serverLogger.LogError(e.getMessage());
			}
			return ResponseBuilder.THX(order, "NSF");
		}
		if (noSuchArmy()) {
			//should never occur
			try {
				throw new RuntimeException("No such army returned true. Ensure the Diplomacy Server has been adequately expanded to cope with this scenario");
			} catch (Exception e) {
				manager.serverLogger.LogError(e.getMessage());
			}
			return ResponseBuilder.THX(order, "NSA");
		}
		return ResponseBuilder.THX(order, "MBV");
	}

	static private boolean notRightSeason(Order order, Game game) {
		Phase phase = game.getPhase();
		if (phase == Phase.SPR || phase == Phase.FAL) {
			if (order instanceof RTOOrder || order instanceof DSBOrder || order instanceof BLDOrder
					|| order instanceof REMOrder || order instanceof WVEOrder) {
				return true;
			}
		}
		if (phase == Phase.SUM || phase == Phase.AUT) {
			if (!(order instanceof RTOOrder) && !(order instanceof  DSBOrder)) {
				return true;
			}
		}
		if (phase == Phase.WIN) {
			if (!(order instanceof BLDOrder) && !(order instanceof REMOrder) &&  !(order instanceof WVEOrder)) {
				return true;
			}
		}
		return false;
	}

	static private boolean noMoreRemovalsAllowed(Order order, GameManager gameManager) {
		if (!(order instanceof REMOrder)) {
			return false;
		}
		int scs = order.getPower().getOwnedSCs().size();
		int regions = order.getPower().getControlledRegions().size();
		int diff = scs - regions;
		if (diff > 0) {
			return true;
		}
		else {
			diff = Math.abs(diff);
			for (Region region: order.getPower().getControlledRegions()) {
				Order orderForRegion = gameManager.getOrderForRegion(region);
				if (orderForRegion instanceof REMOrder) {
					diff--;
				}
			}
			if (diff <= 0) {
				return true;
			}
		}
		return false;
	}

	static private boolean noMoreBuildsAllowed(Order order, GameManager gameManager) {
		if (!(order instanceof BLDOrder)) {
			return false;
		}
		int scs = order.getPower().getOwnedSCs().size();
		int regions = order.getPower().getControlledRegions().size();
		int diff = scs - regions;
		if (diff < 0) {
			return true;
		}
		else {
			for (Region region: order.getPower().getControlledRegions()) {
				Order orderForRegion = gameManager.getOrderForRegion(region);
				if (orderForRegion instanceof BLDOrder) {
					diff--;
				}
			}
			if (diff <= 0) {
				return true;
			}
		}
		return false;
	}

	static private boolean notAppropriateBuildLocation(Order order) {
		if (!(order instanceof BLDOrder)) {
			return false;
		}
		if (order.getLocation().getName().contains("AMY")) {
			HashMap<String, String> inlandSCs = Constants.dictionary.getCategory("51");
			if (inlandSCs.containsValue(order.getLocation().getProvince().getName())){
				return false;
			}
			HashMap<String, String> coastalSCs = Constants.dictionary.getCategory("55");
			if (coastalSCs.containsValue(order.getLocation().getProvince().getName())){
				return false;
			}
			HashMap<String, String> bicoastalSCs = Constants.dictionary.getCategory("57");
			if (bicoastalSCs.containsValue(order.getLocation().getProvince().getName())){
				return false;
			}
		}
		else if (order.getLocation().getName().contains("FLT")) {
			if (order.getLocation().getProvince().getName().compareTo("STP") == 0) {
				return true; // no coast specified
			}
			HashMap<String, String> seaSCs = Constants.dictionary.getCategory("53");
			if (seaSCs == null) {
				return false;
			}
			if (seaSCs.containsValue(order.getLocation().getProvince().getName())){
				return false;
			}
			HashMap<String, String> coastalSCs = Constants.dictionary.getCategory("55");
			if (coastalSCs.containsValue(order.getLocation().getProvince().getName())){
				return false;
			}
		}
		else {
			if (order.getLocation().getProvince().getName().compareTo("STP") == 0) {
				return false;
			}
		}
		return true;
	}

	static private boolean notSupplyCentre(Order order, ConnectionHandler connectionHandler, Game game) {
		if (!(order instanceof BLDOrder)) {
			return false;
		}
		if (!game.getProvince(order.getLocation().getProvince().getName()).isSC()) {
			return true;
		}
		return false;
	}

	static private boolean notHomeSupplyCentre(Order order, ConnectionHandler connectionHandler, Game game) {
		if (!(order instanceof BLDOrder)) {
			return false;
		}
		if (!game.getPower(connectionHandler.getPowerName()).isOwning(order.getLocation().getProvince())) {
			return true;
		}
		return false;
	}

	static private boolean notEmptySupplyCentre(Order order, ConnectionHandler connectionHandler, Game game) {
		if (!(order instanceof BLDOrder)) {
			return false;
		}
		if (game.getPower(connectionHandler.getPowerName()).isControlling(order.getLocation().getProvince())) {
			return true;
		}
		return false;
	}

	static private boolean notYourSupplyCentre(Order order, ConnectionHandler connectionHandler, Game game) {
		if (!(order instanceof BLDOrder)) {
			return false;
		}
		if (!game.getPower(connectionHandler.getPowerName()).isOwning(order.getLocation().getProvince())) {
			return true;
		}
		return false;
	}

	static private boolean notValidRetreatSpace(Order order, Game game) {
		RTOOrder rtoOrder = null;
		if (order instanceof RTOOrder) {
			rtoOrder = (RTOOrder) order;
		}
		else {
			return false;
		}
		Dislodgement dislodgement = game.getDislodgedRegions().get(order.getLocation());
		if (dislodgement == null) {
			return false;
		}
		if (!dislodgement.getRetreateTo().contains(rtoOrder.getDestination())) {
			return true;
		}
		return false;
	}

	static private boolean noRetreatNeeded(Order order, Game game) {
		if ((order instanceof RTOOrder) || (order instanceof DSBOrder)) {
			if (game.getDislodgedRegions().get(order.getLocation()) == null) {
				return true;
			}
		}
		return false;
	}

	static private boolean notYourUnit(Order order, ConnectionHandler connectionHandler, Game game) {
		if (order instanceof WVEOrder) {
			return false;
		}
		if (order.getPower().getName().compareTo(connectionHandler.getPowerName()) != 0) {
			return true;
		}
		if (order instanceof BLDOrder) {
			return false;
		}
		HashMap<Region, Dislodgement> dislodgementHashMap= game.getDislodgedRegions();
		if (dislodgementHashMap != null) {
			Dislodgement dislodgement = dislodgementHashMap.get(order.getLocation());
			if (dislodgement != null) {
				if (dislodgement.getPower().equals(order.getPower())) {
					return false;
				}
			}
		}
		if (!game.getPower(connectionHandler.getPowerName()).isControlling(order.getLocation())) {
			return true;
		}
		return false;
	}

	static private boolean noSuchUnit(Order order, Game game) {
		if (order instanceof WVEOrder) {
			return false;
		}
		if (order.getLocation() == null) {
			return true;
		}
		if (game.getRegion(order.getLocation().getName()) == null) {
			return true;
		}
		if (order instanceof MTOOrder) {
			Region destination = ((MTOOrder) order).getDestination();
			if (game.getRegion(destination.getName()) == null) {
				return true;
			}
		}
		if (order instanceof RTOOrder) {
			Region destination = ((RTOOrder) order).getDestination();
			if (game.getRegion(destination.getName()) == null) {
				return true;
			}
		}
		return false;
	}

	static private boolean noSuchProvince(Order order) {
		if (order instanceof WVEOrder) {
			return false;
		}
		if (order.getLocation().getProvince() == null) {
			return true;
		}
		if (order instanceof MTOOrder) {
			if (((MTOOrder) order).getDestination().getProvince() == null) {
				return true;
			}
		}
		if (order instanceof SUPMTOOrder) {
			if (((SUPMTOOrder) order).getSupportedOrder() == null) {
				return true;
			}
			if (((SUPMTOOrder) order).getSupportedOrder().getLocation() == null) {
				return true;
			}
			if (((SUPMTOOrder) order).getSupportedOrder().getDestination() == null) {
				return true;
			}
			if (((SUPMTOOrder) order).getSupportedOrder().getLocation().getProvince() == null) {
				return true;
			}
			if (((SUPMTOOrder) order).getSupportedOrder().getDestination().getProvince() == null) {
				return true;
			}
		}
		if (order instanceof SUPOrder) {
			if (((SUPOrder) order).getSupportedOrder().getLocation().getProvince() == null) {
				return true;
			}
		}
		if (order instanceof RTOOrder) {
			if (((RTOOrder) order).getDestination().getProvince() == null) {
				return true;
			}
		}
		return false;
	}

	static private boolean notAdjacent(Order order, Game game) {
		if (order instanceof MTOOrder) {
			Region location = order.getLocation();
			Region regionDestination = ((MTOOrder) order).getDestination();
			if (regionDestination != null) {
				if (!location.getAdjacentRegions().contains(regionDestination)) {
					return true;
				}
			}
			else {
				return true;
			}
		}
		if (order instanceof RTOOrder) {
			Region location = order.getLocation();
			Region destination = ((RTOOrder) order).getDestination();
			if (!location.getAdjacentRegions().contains(destination)) {
				return true;
			}
		}
		if (order instanceof SUPMTOOrder) {
			if (order.getLocation().equals(((SUPMTOOrder) order).getSupportedOrder().getLocation())){
				return true;
			}
			if (order.getLocation().equals(((SUPMTOOrder) order).getSupportedOrder().getDestination())){
				return true;
			}
		}
		if (order instanceof SUPOrder) {
			if (order.getLocation().equals(((SUPOrder) order).getSupportedOrder().getLocation())) {
				return true;
			}
		}
		return false;
	}

	static private boolean notAtSea() {
		//Always false as this is only used in CVY and CTO orders which should not occur
		return false;
	}

	static private boolean noSuchFleet() {
		//Always false as this is only used in CVY and CTO orders which should not occur
		return false;
	}

	static private boolean noSuchArmy() {
		//Always false as this is only used in CVY and CTO orders which should not occur
		return false;
	}

	static private Turn getNextTurn(Turn currentTurn) {
		Turn nextTurn = currentTurn;
		switch (currentTurn.getPhase()) {
			case SPR:
				nextTurn.setPhase(Phase.SUM);
				break;
			case SUM:
				nextTurn.setPhase(Phase.FAL);
				break;
			case FAL:
				nextTurn.setPhase(Phase.AUT);
				break;
			case AUT:
				nextTurn.setPhase(Phase.WIN);
				break;
			case WIN:
				nextTurn.setPhase(Phase.SPR);
				nextTurn.setYear(currentTurn.getYear() + 1);
				break;
			default:
				nextTurn = null;
				break;
		}
		return nextTurn;
	}

	static private Turn getPreviousTurn(Turn currentTurn) {
		Turn previousTurn = currentTurn;
		switch (currentTurn.getPhase()) {
			case SPR:
				previousTurn.setPhase(Phase.WIN);
				previousTurn.setYear(currentTurn.getYear() - 1);
				break;
			case SUM:
				previousTurn.setPhase(Phase.SPR);
				break;
			case FAL:
				previousTurn.setPhase(Phase.SUM);
				break;
			case AUT:
				previousTurn.setPhase(Phase.FAL);
				break;
			case WIN:
				previousTurn.setPhase(Phase.AUT);
				break;
			default:
				previousTurn = null;
				break;
		}
		return previousTurn;
	}

	public static void main(String args[]) {
		//SND(PHASE YR)(POW POW...)(PRP(DRW))
		GameInfo gameInfo = new GameInfo("testgame");
		ServerLogger serverLogger = new ServerLogger();
		GameManager manager = new GameManager(gameInfo, serverLogger);

		Turn turn = new Turn(1901, Phase.SPR);
		int k = 1;
		for (int i = 0; i < 7; i++) {
			List<Message> messageList = new ArrayList<>();
			for (int j = 0; j < i + 1; j++) {
				messageList.add(ResponseBuilder.DRW());
			}
			//System.out.println(manager.orderHistoryHasKey(turn.toString()) + Integer.toString(turn.getYear()) + turn.getPhase().name());
			List<Message> rep = manager.addTurnHistory(turn.toString(), messageList);
			if (rep != null) {
				//System.out.println("Replacing last entry");
			}
			turn = getNextTurn(turn);
		}
		turn = getPreviousTurn(turn);
		for (int i = 0; i < 7; i++) {
			int turn1 = manager.getTurnHistory(turn.toString()).size();
			//System.out.println(turn.toString() + turn1);
			turn = getPreviousTurn(turn);
		}
	}
}