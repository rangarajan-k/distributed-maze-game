import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

public interface GameService extends Remote{

//	public int addPlayer(SinglePlayerGameStates p) throws RemoteException;
	
	public int addNewPlayer(SinglePlayerGameStates p) throws RemoteException;
	
	public SinglePlayerGameStates addPlayer(String id, String ip, String portNumber ) throws RemoteException;
	
	public int	dropPlayer(String playerKey) throws RemoteException;
	
	public int movePlayer(String playerKey,String move) throws RemoteException;
	
	public Hashtable<String, SinglePlayerGameStates> getGameStatus() throws RemoteException;
	
	public String[][] getGameMap() throws RemoteException;
	
	public GameState getGameState() throws RemoteException;
}
