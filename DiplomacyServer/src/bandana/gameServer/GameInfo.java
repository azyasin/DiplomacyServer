package bandana.gameServer;

import java.util.HashMap;


/**
 * This class collects all the parameters that can be set for any game. <br/>
 * <p>
 * When the server starts a new game, the parameters of that game should first be set in an object of this class. Next, the server will use this object to initialize a GameManager.
 *
 * @author Dave de Jonge, Western Sydney University
 */
public class GameInfo implements java.io.Serializable {


	//STATIC METHODS

	//FIELDS

	/**
	 * Identifier of the game (for debugging, logging, and data analysis purposes)
	 */
	private String gameID;

	/**
	 * The year after which the server should declare a draw.
	 */
	private int finalYear = Integer.MAX_VALUE;

	private boolean egalitarian = true;

	private int moveTimeLimit = 30;    //time limit, in seconds, for spring and fall phases.
	private int retreatTimeLimit = 5;    //time limit in seconds, for summer and autumn phases
	private int buildTimeLimit = 5;    //time limit in seconds for winter phases.
	private int level = 10;

	private boolean DSD = false;
	private boolean AOA = false;
	private boolean PDA = true;
	private boolean NPR = false;
	private boolean NPB = false;
	private int PTL = 0;


	//CONSTRUCTORS
	public GameInfo(String gameID) {
		this.gameID = gameID;
	}

	//METHODS

	//GETTERS AND SETTERS


	public String getGameID() {
		return gameID;
	}

	/**
	 * Returns the year after which the server should declare a draw.
	 */
	public int getFinalYear() {
		return finalYear;
	}


	/**
	 * Sets the year after which the server should declare a draw.
	 */
	public void setFinalYear(int finalYear) {
		this.finalYear = finalYear;
	}

	public int getMoveTimeLimit() {
		return moveTimeLimit;
	}


	public void setMoveTimeLimit(int moveTimeLimit) {
		this.moveTimeLimit = moveTimeLimit;
	}

	public int getRetreatTimeLimit() {
		return retreatTimeLimit;
	}

	public void setRetreatTimeLimit(int retreatTimeLimit) {
		this.retreatTimeLimit = retreatTimeLimit;
	}

	public int getBuildTimeLimit() {
		return buildTimeLimit;
	}

	public void setBuildTimeLimit(int buildTimeLimit) {
		this.buildTimeLimit = buildTimeLimit;
	}

	public void setLevel(int level) {
		this.level = level;
		if (this.level == 0) {
			this.PDA = false;
			this.NPB = false;
			this.NPR = false;
			this.PTL = 0;
		}
	}

	public int getLevel() {
		return level;
	}

	public boolean isAOA() {
		return AOA;
	}

	public void setAOA(boolean AOA) {
		this.AOA = AOA;
	}

	public boolean isDSD() {
		return DSD;
	}

	public void setDSD(boolean DSD) {
		this.DSD = DSD;
	}

	public boolean isNPR() {
		return NPR;
	}

	public void setNPR(boolean NPR) {
		this.NPR = NPR;
	}

	public boolean isNPB() {
		return NPB;
	}

	public void setNPB(boolean NPB) {
		this.NPB = NPB;
	}

	public boolean isPDA() {
		return PDA;
	}

	public void setPDA(boolean PDA) {
		this.PDA = PDA;
	}

	public int getPTL() {
		return PTL;
	}

	public void setPTL(int PTL) {
		this.PTL = PTL;
	}

	public boolean isEgalitarian() {
		return egalitarian;
	}

	public void setEgalitarian(boolean egalitarian) {
		this.egalitarian = egalitarian;
	}
}
