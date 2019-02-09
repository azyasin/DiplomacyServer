package bandana.gameServer;

import es.csic.iiia.fabregues.dip.board.Power;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProposedDraw {
	List<String> includedPowers;

	public ProposedDraw(List<String> powersList) {
		includedPowers = powersList;
		Collections.sort(includedPowers);
	}

	public ProposedDraw() {
		includedPowers = new ArrayList<>();
	}

	public List<String> getIncludedPowers() {
		return includedPowers;
	}

	public void setIncludedPowers(List<String> includedPowers) {
		this.includedPowers = includedPowers;
		Collections.sort(this.includedPowers);
	}

	public void resetIncludedPowers() {
		includedPowers.clear();
	}

	@Override
	public String toString() {
		String powerList = "";
		for (String power: this.includedPowers) {
			powerList += power;
		}
		return powerList;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProposedDraw) {
			if (obj.toString().equals(toString())) {
				return true;
			}
		}
		return false;
	}
}
