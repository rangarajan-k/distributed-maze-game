import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MazeBackupServer {
	private String mazeBackupServerIp;
	private String mazeBackupServerPort;
	private BackupEngine mazeGameBackupEngine;
	private BackupService stub = null;
	private Registry registry = null;
	
	public MazeBackupServer(String mazeBackupServerIp, String mazeServerPort) {
		this.mazeBackupServerIp = mazeBackupServerIp;
		this.mazeBackupServerPort = mazeServerPort;
			
		try {
			//Initialize the mazeGame Engine
			this.mazeGameBackupEngine = new BackupEngine();
			stub = (BackupService) UnicastRemoteObject.exportObject(this.mazeGameBackupEngine, 0);
			registry = LocateRegistry.getRegistry();
			this.unbindFromBackupService();
			registry.bind("GameBackupService", stub);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public void unbindFromBackupService() {
		try {
			
			registry.unbind("GameBackupService");
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
}
