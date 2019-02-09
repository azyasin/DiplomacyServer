package bandana.gameServer;

import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Power;

import javax.print.DocFlavor;
import java.util.HashMap;
import java.util.List;

public class GameLoggingState implements java.io.Serializable{
	private Game game;
	private List<Power> powersInCivillDisorder;
	private HashMap<String, List<Message>> orderHistory;

	public GameLoggingState(Game game, List<Power> powersInCivillDisorder, HashMap<String, List<Message>> orderHistory) {
		this.game = game;
		this.orderHistory = orderHistory;
		this.powersInCivillDisorder = powersInCivillDisorder;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public HashMap<String, List<Message>> getOrderHistory() {
		return orderHistory;
	}

	public void setOrderHistory(HashMap<String, List<Message>> orderHistory) {
		this.orderHistory = orderHistory;
	}

	public List<Power> getPowersInCivillDisorder() {
		return powersInCivillDisorder;
	}

	public void setPowersInCivillDisorder(List<Power> powersInCivillDisorder) {
		this.powersInCivillDisorder = powersInCivillDisorder;
	}
}
