import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.LinkedList;

public class BackupEngine implements BackupService{
	
	BackupGameState gameState = new BackupGameState();

	@Override
	public void updateGameState(Hashtable<String, SinglePlayerGameStates> playerStats, String[][] gameMap, GameState gs) throws RemoteException{
		//System.out.println("Player updating GameState");
		
		gameState.setPlayerStats(playerStats);
		gameState.setGameMap(gameMap);
		gameState.setGameState(gs);
	}

	@Override
	public BackupGameState getGameState() throws RemoteException {
		
		System.out.println("GameState is not empty Length : "+gameState.getGameMap().length);
		// TODO Auto-generated method stub
		return gameState;
	}
	
	
}
