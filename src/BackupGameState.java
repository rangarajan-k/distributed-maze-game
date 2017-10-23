import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

public class BackupGameState implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String[][] gameMap;
	Hashtable<String, SinglePlayerGameStates> playerStats =
            new Hashtable<String, SinglePlayerGameStates>();
	
	Map<String, Integer> playerAttendance = new HashMap<String, Integer>();
	
	private GameState gs;
	
	public String[][] getGameMap() {
		return gameMap;
	}
	public void setGameMap(String[][] gameMap) {
		this.gameMap = gameMap;
	}
	public void setGameState(GameState gs){
		this.gs = gs;
	}
	public GameState getGameState(){
		return gs;
	}
	public Hashtable<String, SinglePlayerGameStates> getPlayerStats() {
		return playerStats;
	}
	public void setPlayerStats(Hashtable<String, SinglePlayerGameStates> playerStats) {
		this.playerStats = playerStats;
	}
	public Map<String, Integer> getPlayerAttendance() {
		return playerAttendance;
	}
	public void setPlayerAttendance(Map<String, Integer> playerAttendance) {
		this.playerAttendance = playerAttendance;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
