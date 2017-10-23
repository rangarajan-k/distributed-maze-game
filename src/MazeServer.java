import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Map;

/*
 * MazeServer is the server component of Game Engine
 * it is instantiated only if the Game generates itself as the primary/backup server
 * 
 * it contains
 * 
 * ip
 * GameState 
 */
public class MazeServer{
	private String mazeServerIp;
	private String mazeServerPort;
	private GameEngine mazeGameEngine;
	
	public MazeServer(String trackerIp,String mazeServerIp, String mazeServerPort, Integer gridN, Integer  treasureK, String[][] gameMap, Hashtable<String, SinglePlayerGameStates> playerStats, Map<String, Integer> playerAttendance,GameState gs, int removeDeadPrimary) {
		this.mazeServerIp = mazeServerIp;
		this.mazeServerPort = mazeServerPort;
		
		GameService stub = null;
		Registry registry = null;
			
		try {
			//Initialize the mazeGame Engine
			
			this.mazeGameEngine = new GameEngine(trackerIp,gridN, treasureK, gameMap, playerStats, playerAttendance,gs,removeDeadPrimary);
			stub = (GameService) UnicastRemoteObject.exportObject(this.mazeGameEngine, 0);
			registry = LocateRegistry.getRegistry();
			System.out.println("Attempting to bind primary rmi");
			registry.bind("GameService", stub);
			System.out.println("Primary rmi successfully binded");
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}

	
}
