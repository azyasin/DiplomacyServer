package bandana.gameServer;

import es.csic.iiia.fabregues.dip.board.Phase;

import java.util.List;

public class Turn implements java.io.Serializable{
	private int year;
	private Phase phase;

	public Turn(int year, Phase phase){
		this.year = year;
		this.phase = phase;
	}

	public int getYear() {
		return year;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setPhase(Phase phase) {
		this.phase = phase;
	}

	public void setYear(int year) {
		this.year = year;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Turn) {
			if (((Turn) obj).getPhase() == phase && ((Turn) obj).getYear() == year) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return Integer.toString(year) + phase.name();
	}
}
