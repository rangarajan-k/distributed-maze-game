import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.LinkedList;

public interface GameService extends Remote{

//	public int addPlayer(SinglePlayerGameStates p) throws RemoteException;
	
	public int addNewPlayer(SinglePlayerGameStates p) throws RemoteException;
	
	public SinglePlayerGameStates addPlayer(String id, String ip, String portNumber ) throws RemoteException;
	
	public int	dropPlayer(String playerKey) throws RemoteException;
	
	public int movePlayer(String playerKey,String move) throws RemoteException;
	
	public Hashtable<String, SinglePlayerGameStates> getGameStatus() throws RemoteException;
	
	public String[][] getGameMap() throws RemoteException;
	
	public void setGameStatus(Hashtable<String, SinglePlayerGameStates> playerStats) throws RemoteException;
	
	public void setGameMap(String[][] gameMap) throws RemoteException;
	
	public void updatePresense(String playerId) throws RemoteException;
	
	public GameState getGameState() throws RemoteException;
	
	public SinglePlayerGameStates getPlayerState(String id) throws RemoteException;
	
	public int getPlayerOrder(String id) throws RemoteException;
	
	LinkedList<String> getPlayerOrderList() throws RemoteException;
	
	public void setBackupService(String ip) throws RemoteException;
	
	public void updateBackupServer() throws RemoteException;
	
	public boolean iAmAlive() throws RemoteException;
}
