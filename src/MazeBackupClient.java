import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Hashtable;

public class MazeBackupClient {
	String backupServerIp;
	String backupServerPort;
	private static BackupService look_up_backup_service;
	
	public MazeBackupClient(String backupServerIp, String backupServerPort) throws MalformedURLException, RemoteException, NotBoundException {
		this.backupServerIp = backupServerIp;
		this.backupServerPort = backupServerPort;
		
		look_up_backup_service = (BackupService) Naming.lookup("//"+backupServerIp+"/GameBackupService");
	}
	
	public void updateBackupServer(String[][] gameMap, Hashtable<String, SinglePlayerGameStates> playerStats,GameState gs) {
		try {
			System.out.println("Updating Backup");
			look_up_backup_service.updateGameState(playerStats, gameMap,gs);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
