package bandana.gameServer;

import java.util.ArrayList;
import java.util.List;

public class GameHistory implements java.io.Serializable{
	private GameInfo gameInfo;
	private List<GameLoggingState> gameLoggingStates;

	public GameHistory(GameInfo gameInfo, List<GameLoggingState> gameLoggingStates) {
		this.gameInfo = gameInfo;
		this.gameLoggingStates = gameLoggingStates;
	}

	public GameHistory(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
		gameLoggingStates = new ArrayList<>();
	}

	public GameInfo getGameInfo() {
		return gameInfo;
	}

	public void setGameInfo(GameInfo gameInfo) {
		this.gameInfo = gameInfo;
	}

	public List<GameLoggingState> getGameLoggingStates() {
		return gameLoggingStates;
	}

	public void setGameLoggingStates(List<GameLoggingState> gameLoggingStates) {
		this.gameLoggingStates = gameLoggingStates;
	}

	public void addGameLoggingState(GameLoggingState gameLoggingState) {
		gameLoggingStates.add(gameLoggingState);
	}

	public boolean removeGameLoggingState(GameLoggingState gameLoggingState) {
		return gameLoggingStates.remove(gameLoggingState);
	}
}
