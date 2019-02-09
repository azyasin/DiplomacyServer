package bandana.gameServer;

import es.csic.iiia.fabregues.dip.board.Power;

public class HLOLogin {
	private Power power;
	private int passcode;
	private String clientName;
	private String clientVersion;

	public HLOLogin(Power power, int passcode, String clientName, String clientVersion) {
		this.passcode = passcode;
		this.power = power;
		this.clientName = clientName;
		this.clientVersion = clientVersion;
	}

	public Power getHLOPower() {
		return power;
	}

	public int getPasscode() {
		return passcode;
	}

	public String getClientName() {
		return clientName;
	}

	public String getClientVersion() {
		return clientVersion;
	}
}
