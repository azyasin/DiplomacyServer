package bandana.gameServer;

import ddejonge.bandana.gameBuilder.DiplomacyGameBuilder;
import es.csic.iiia.fabregues.dip.board.*;
import es.csic.iiia.fabregues.dip.orders.*;

import java.util.*;

public class ResponseBuilder {

	static Message HLO(String power, int passcode, GameInfo gameInfo) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("HLO"));

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power));
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(passcode));
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("LVL"));
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(gameInfo.getLevel()));
		buffer = addBytesToList(buffer, closingBracket());
		if (gameInfo.getMoveTimeLimit() > 0) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MTL"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexNum(gameInfo.getMoveTimeLimit()));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.getRetreatTimeLimit() > 0) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("RTL"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexNum(gameInfo.getRetreatTimeLimit()));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.getBuildTimeLimit() > 0) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("BTL"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexNum(gameInfo.getBuildTimeLimit()));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.isDSD()) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("DSD"));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.isAOA()) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("AOA"));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.isPDA()) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("PDA"));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.isNPR()) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("AOA"));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.isNPB()) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("AOA"));
			buffer = addBytesToList(buffer, closingBracket());
		}
		if (gameInfo.getPTL() > 0) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("PTL"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexNum(gameInfo.getPTL()));
			buffer = addBytesToList(buffer, closingBracket());
		}
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte) 2, resp);
	}

	static Message MAP(String mapName) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MAP"));
		buffer = addBytesToList(buffer, openingBracket());
		for (int i = 0; i < mapName.length(); i++) {
			buffer = addBytesToList(buffer, TokenTranslator.toAsciiHex(mapName.charAt(i)));
		}
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte) 2, resp);
	}

	static Message MDF(Game game) {
		List<Byte> buffer = new ArrayList<>();
		Vector<Province> provinceVector = game.getProvinces();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MDF"));

		buffer = addBytesToList(buffer, openingBracket()); //Powers
		for (Power power: game.getPowers()) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));
		}
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket()); // Provinces - ((Supply Centres)(Non-Supply Centres))
		buffer = addBytesToList(buffer, openingBracket()); // Supply Centres - (power centre centre …)

		for (Power power: game.getPowers()) { //Owned Scs
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));
			for (Province province: power.getHomes()) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(province.getName()));
				provinceVector.remove(province);
			}
			buffer = addBytesToList(buffer, closingBracket());
		}

		buffer = addBytesToList(buffer, openingBracket()); //Unowned SCs
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("UNO"));
		for (Province province: provinceVector) {
			if (province.isSC()) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(province.getName()));
			}
		}
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, closingBracket());
		buffer = addBytesToList(buffer, openingBracket()); // Non-Supply Centres
		for (Province province: provinceVector) {
			if (!province.isSC()) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(province.getName()));
			}
		}
		buffer = addBytesToList(buffer, closingBracket());
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket()); //Adjacency
		for (Province province: game.getProvinces()) {
			String[] types = {"AMY", "FLT", "NCS", "NEC", "ECS", "SEC", "SCS", "SWC", "WCS", "NWC"};

			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(province.getName()));

			for (String unitType: types) {
				Region region = game.getRegion(province.getName()+unitType);
				if (region != null) {
					buffer = addBytesToList(buffer, openingBracket());
					if (unitType.equals("FLT") || unitType.equals("AMY")) {
						buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(unitType));
					}
					else {
						buffer = addBytesToList(buffer, openingBracket());
						buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
						buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(unitType));
						buffer = addBytesToList(buffer, closingBracket());
					}
					for (Region adjacency: region.getAdjacentRegions()) {
						if (!adjacency.getName().contains("FLT") && !adjacency.getName().contains("AMY")) {
							buffer = addBytesToList(buffer, openingBracket());
							buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(adjacency.getProvince().getName()));
							buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(adjacency.getName().substring(3)));
							buffer = addBytesToList(buffer, closingBracket());
						}
						else {
							buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(adjacency.getProvince().getName()));
						}
					}
					buffer = addBytesToList(buffer, closingBracket());
				}
			}

			buffer = addBytesToList(buffer, closingBracket());
		}
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte) 2, resp);
	}

	static Message SCO(List<Power> powers, List<Province> allProvinces) {
		List<Byte> buffer = new ArrayList<>();
		List<Province> unownedSCs = new ArrayList<>();
		for (Province province: allProvinces) {
			if (province.isSC()) {
				unownedSCs.add(province);
			}
		}
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("SCO"));

		for (Power power: powers) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));

			for (Province province: power.getOwnedSCs()) {
				if (province != null && province.isSC()) {
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(province.getName()));
					unownedSCs.remove(province);
				}
			}

			buffer = addBytesToList(buffer, closingBracket());
		}

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("UNO"));

		for (Province province: unownedSCs) {
			if (province != null && province.isSC()) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(province.getName()));
			}
		}

		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];

		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}

		return new Message((byte) 2, resp);
	}

	static Message NOW(List<Power> powers, int year, Phase phase, Game game) {
		List<Region> completeMRTs = new ArrayList<>();
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("NOW"));

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(phase.name()));
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(year));
		buffer = addBytesToList(buffer, closingBracket());

		for (Power power: powers) {
			if (power != null) {
				for (Region region: power.getControlledRegions()) {
					buffer = addBytesToList(buffer, openingBracket());
					buffer = addRegion(buffer, region, power);
					int lenBeforMRT = buffer.size();
					buffer = addMRT(buffer, region, game.getDislodgedRegions(), power);
					if (buffer.size() > lenBeforMRT) {
						completeMRTs.add(region);
					}
					buffer = addBytesToList(buffer, closingBracket()); // add final closing bracket
				}
				for (Region region: game.getDislodgedRegions(power)){
					if (!completeMRTs.contains(region)) {
						buffer = addBytesToList(buffer, openingBracket());
						buffer = addRegion(buffer, region, power);
						int lenBeforMRT = buffer.size();
						buffer = addMRT(buffer, region, game.getDislodgedRegions(), power);
						if (buffer.size() > lenBeforMRT) {
							completeMRTs.add(region);
						}
						buffer = addBytesToList(buffer, closingBracket()); // add final closing bracket
					}
				}
			}
		}

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte) 2, resp);
	}

	static public Message THX(Order order, String note){
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("THX"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addOrder(order, buffer);
		buffer = addBytesToList(buffer, closingBracket());
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(note));
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message MIS(int numBuilds){
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MIS"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(numBuilds));
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message MIS(HashMap<Region, Dislodgement> missingdislodgementHashMap, Power power){
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MIS"));
		Set<Region> keys = missingdislodgementHashMap.keySet();
		if (keys != null) {
			if (keys.size() > 0) {
				for (Region region: keys) {
					Dislodgement dislodgement = missingdislodgementHashMap.get(region);
					buffer = addBytesToList(buffer, openingBracket());
					buffer = addRegion(buffer, region, power);
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MRT"));
					for (Region retreatToRegion: dislodgement.getRetreateTo()){
						buffer = addMDFProvince(buffer, retreatToRegion);
					}
					buffer = addBytesToList(buffer, closingBracket());
				}
			}
		}
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message MIS(List<Region> missing, Power power){
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MIS"));
		if (missing != null) {
			if (missing.size() > 0) {
				for (Region region: missing) {
					buffer = addBytesToList(buffer, openingBracket());
					buffer = addRegion(buffer, region, power);
					buffer = addBytesToList(buffer, closingBracket());
				}
			}
		}
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message OFF() {
		return new Message((byte)2, new byte[] {(byte)0x48, (byte)0x10});
	}

	static public Message ORD(Phase phase, int year, Order order, String result, boolean RET) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("ORD"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(phase.name()));
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(year));
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addOrder(order, buffer);
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(result));
		if (RET) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("RET"));
		}
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message YES(byte[] previousMessage) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("YES"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, previousMessage);
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message PRN(byte[] previousMessage) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("PRN"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, previousMessage);
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message REJ(byte[] previousMessage) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("REJ"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, previousMessage);
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message NOT(byte[] previousMessage) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("NOT"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, previousMessage);
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message HUH(byte[] previousMessage, int location) {
		List<Byte> buffer = new ArrayList<>();
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("HUH"));
		buffer = addBytesToList(buffer, openingBracket());
		for(int i = 0; i < previousMessage.length; i++) {
			if (i == location) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("ERR"));
			}
			buffer.add(previousMessage[i]);
		}
		if (location > previousMessage.length) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("ERR"));
		}
		buffer = addBytesToList(buffer, closingBracket());
		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message DRW() {
		return new Message((byte)2, new byte[]{(byte)0x48, (byte)0x01});
	}

	static public Message TME(int diff) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("TME"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(diff));
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message CCD(Power power) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("CCD"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message OUT(Power power) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("OUT"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message SLO(Power power) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("SLO"));
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message SMR(Game game, List<ConnectionHandler> playerHandlers) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("SMR"));
		//TURN = (SEASON YEAR)
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(game.getPhase().name()));
		buffer = addBytesToList(buffer, TokenTranslator.toHexNum(game.getYear()));
		buffer = addBytesToList(buffer, closingBracket());
		for (ConnectionHandler connectionHandler: playerHandlers) {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(connectionHandler.getPowerName()));
			buffer = addBytesToList(buffer, openingBracket());
			int length = connectionHandler.getClientName().length();
			for (int i = 0; i < length; i++) { //add client name
				buffer = addBytesToList(buffer, TokenTranslator.toAsciiHex(connectionHandler.getClientName().charAt(i)));
			}
			buffer = addBytesToList(buffer, closingBracket());
			buffer = addBytesToList(buffer, openingBracket());
			length = connectionHandler.getClientVersion().length();
			for (int i = 0; i < length; i++) { //add client version
				buffer = addBytesToList(buffer, TokenTranslator.toAsciiHex(connectionHandler.getClientVersion().charAt(i)));
			}
			buffer = addBytesToList(buffer, closingBracket());
			int scs = game.getPower(connectionHandler.getPowerName()).getOwnedSCs().size();
			if (scs > 0) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexNum(scs));
			}
			else {
				buffer = addBytesToList(buffer, TokenTranslator.toHexNum(0));
				if (connectionHandler.getEliminationYear() > 0) {
					buffer = addBytesToList(buffer, TokenTranslator.toHexNum(connectionHandler.getEliminationYear()));
				}
				else {
					buffer = addBytesToList(buffer, TokenTranslator.toHexNum(game.getYear()));
				}
			}
			buffer = addBytesToList(buffer, closingBracket());
		}

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static public Message FRM(String sender, List<String> recipients, byte[] content) {
		List<Byte> buffer = new ArrayList<>();

		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FRM"));

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(sender));
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket());
		for (String recipient: recipients) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(recipient));
		}
		buffer = addBytesToList(buffer, closingBracket());

		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, content);
		buffer = addBytesToList(buffer, closingBracket());

		byte[] resp = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			resp[i] = buffer.get(i);
		}
		return new Message((byte)2, resp);
	}

	static private List<Byte> addMRT(List<Byte> buffer, Region region, HashMap<Region, Dislodgement> dislodgementHashMap, Power power){
		Dislodgement dislodgement = dislodgementHashMap.get(region);
		if(dislodgement == null) {
			return buffer;
		}
		if (!dislodgement.getPower().equals(power)) {
			return buffer;
		}
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MRT"));
		buffer = addBytesToList(buffer, openingBracket());
		for (Region region1: dislodgement.getRetreateTo()) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region1.getProvince().getName()));
		}
		buffer = addBytesToList(buffer, closingBracket());
		return buffer;
	}

	static private List<Byte> addMDFProvince(List<Byte> buffer, Region region){
		if (region.getName().contains("AMY") || region.getName().contains("FLT")) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getProvince().getName()));
		}
		else {
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getProvince().getName()));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getName().substring(3)));
			buffer = addBytesToList(buffer, closingBracket());
		}
		return buffer;
	}

	static private List<Byte> addRegion(List<Byte> buffer, Region region, Power power){
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(power.getName()));
		if (region.getName().contains("AMY")){
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("AMY"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getProvince().getName()));
		}
		else if (region.getName().contains("FLT")) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getProvince().getName()));
		}
		else {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getProvince().getName()));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(region.getName().substring(3)));
			buffer = addBytesToList(buffer, closingBracket());
		}
		return buffer;
	}

	static private List<Byte> addOrder(Order order, List<Byte> buffer) {
		if (order == null) {
			System.out.println("NULL order!!!");
			return buffer;
		}
		if (order instanceof WVEOrder) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(order.getPower().getName()));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("WVE"));
		}
		else {
			buffer = addUnitWithLocationToList(buffer, order);
			if (order instanceof HLDOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("HLD"));
			}
			if (order instanceof MTOOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MTO"));
				if (!((MTOOrder) order).getDestination().getName().contains("AMY") && !((MTOOrder) order).getDestination().getName().contains("FLT")) {
					// province and coast
					buffer = addBytesToList(buffer, openingBracket());
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((MTOOrder) order).getDestination().getProvince().getName()));
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((MTOOrder) order).getDestination().getName().substring(3)));
					buffer = addBytesToList(buffer, closingBracket());
				}
				else {
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((MTOOrder) order).getDestination().getProvince().getName()));
				}
			}
			if (order instanceof SUPOrder || order instanceof  SUPMTOOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("SUP"));
				buffer = addSupportedOrder(buffer, order);
			}
			if (order instanceof RTOOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("RTO"));
				if (((RTOOrder) order).getDestination().getName().contains("AMY") || ((RTOOrder) order).getDestination().getName().contains("FLT")) {
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((RTOOrder) order).getDestination().getProvince().getName()));
				}
				else {
					buffer = addBytesToList(buffer, openingBracket());
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((RTOOrder) order).getDestination().getProvince().getName()));
					buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((RTOOrder) order).getDestination().getName().substring(3)));
					buffer = addBytesToList(buffer, closingBracket());
				}
			}
			if (order instanceof DSBOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("DSB"));
			}
			if (order instanceof BLDOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("BLD"));
			}
			if (order instanceof REMOrder) {
				buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("REM"));
			}
		}
		return buffer;
	}

	static private List<Byte> addUnitWithLocationToList(List<Byte> buffer, Order order) {
		buffer = addBytesToList(buffer, openingBracket());
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(order.getPower().getName()));
		if (order.getLocation().getName().contains("AMY")) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("AMY"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(order.getLocation().getProvince().getName()));
		}
		else if (order.getLocation().getName().contains("FLT")) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(order.getLocation().getProvince().getName()));
		}
		else {
			//has coast
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(order.getLocation().getProvince().getName()));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(order.getLocation().getName().substring(3)));
			buffer = addBytesToList(buffer, closingBracket());
		}
		buffer = addBytesToList(buffer, closingBracket());
		return buffer;
	}

	static private List<Byte> addSupportedOrder(List<Byte> buffer, Order order) {
		buffer = addBytesToList(buffer, openingBracket());
		Order supportedOrder = null;
		if (order instanceof SUPOrder) {
			supportedOrder = ((SUPOrder) order).getSupportedOrder();
		}
		if (order instanceof SUPMTOOrder) {
			supportedOrder = ((SUPMTOOrder) order).getSupportedOrder();
		}
		buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(supportedOrder.getPower().getName()));
		if (supportedOrder.getLocation().getName().contains("AMY")) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("AMY"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(supportedOrder.getLocation().getProvince().getName()));
		}
		else if (supportedOrder.getLocation().getName().contains("FLT")) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(supportedOrder.getLocation().getProvince().getName()));
		}
		else {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("FLT"));
			buffer = addBytesToList(buffer, openingBracket());
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(supportedOrder.getLocation().getProvince().getName()));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(supportedOrder.getLocation().getName().substring(3)));
			buffer = addBytesToList(buffer, closingBracket());
		}
		buffer = addBytesToList(buffer, closingBracket());
		if (supportedOrder instanceof MTOOrder) {
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes("MTO"));
			buffer = addBytesToList(buffer, TokenTranslator.toHexBytes(((MTOOrder) supportedOrder).getDestination().getProvince().getName()));
		}
		return buffer;
	}

	static private byte[] openingBracket() {
		return new byte[]{0x40, 0x00};
	}

	static private byte[] closingBracket() {
		return new byte[]{0x40, 0x01};
	}

	static private List<Byte> addBytesToList(List<Byte> byteList, byte[] bytes) {
		for (byte b: bytes) {
			byteList.add(b);
		}
		return byteList;
	}

	public static void main(String args[]) {
		Game game = new DiplomacyGameBuilder().createDefaultGame();
		Dislodgement dislodgement = new Dislodgement(game.getPower("RUS"), game.getRegion("LVNAMY"));
		for (Region region: dislodgement.getRegion().getAdjacentRegions()){
			boolean owned = false;
			for (Power power: game.getPowers()){
				if(power.isControlling(region)){
					owned = true;
				}
			}
			if (!owned) {
				dislodgement.addRetreateToRegion(region);
			}
		}
		game.addDislodgedRegion(dislodgement.getRegion(), dislodgement);
		Message message = NOW(game.getPowers(), 1902, game.getPhase(), game);
		System.out.println(message.toString());
	}

}
