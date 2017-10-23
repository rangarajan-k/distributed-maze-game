import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.LinkedList;

public interface BackupService extends Remote{
	
	public void updateGameState(Hashtable<String, SinglePlayerGameStates> playerStats, String[][] gameMap, GameState gs) throws RemoteException;

	public BackupGameState getGameState() throws RemoteException;
}
