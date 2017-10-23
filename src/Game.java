/*
 * Game is the main MazeGame engine
 *
 * 1) contacts Tracker first to check if it can join the Game and obtain the existing player list
 * 2) After obtaining the player List it generates it self as either one of
 *    - Special Player (Primary Server)
 *    - Special Player (Backup Server)
 *    - Regular Player
 * 3) If it generates itself as the primary server
 *    - it initializes the gamestate (create gameMap allocate Trasures etc)
 *    - it allocates itself a random position in the gameMap
 *      *gameMap represents the MazeGame board(2-D array) that contains
 *       - grid location x-cordinate
 *       - grid location y-cordinate
 *       - trasure at grid location
 *       - player at grid location
 *    - it listens on a well know ip/port for other players to update gameMap (eg make move etc)
 *    - it listens for other player pings to remove the crashed player data
 *    - ping its neighbor to check its status in the game and
 *      - remove its data if unreachable
 *      - update tracker  
 *    - play the MazeGame(eg make move)
 *4) If it generates itself as the backup server 
 *    - It first contacts the primary server and obtains the gameMap and its initial position in the gameMap
 *    - It listens on a well known ip/port for other players make move
 *    - It contacts primary server to play the MazeGame (eg make move)  
 *    - ping its neighbor to check its status in the game
 *      - accordingly if the peer is unreachable it will contact the primary server
 *    - play the MazeGame(eg make move)
 *   
 *5) If it generates itself as the regular Player
 *    - It first contacts the primary server and obtains the gameMap and its initial position in the gameMap
 *    - ping its neighbor to check its status in the game
 *      - accordingly if the peer is unreachable it will contact the primary server
 *    - play the MazeGame(eg make move)
 */
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;


//import edu.nus.mazegame.trackerservice.TrackerService;

public class Game {
	private static TrackerService look_up;
	private static Integer waitToMarkAttendance = 2000; //every 2 seconds
	private String trackerIp;
	private String trackerPort;
	private Integer gridN;
	private Integer treasureK;
	private String playerIp;
	private String playerPort;
	private String playerId;
	private Integer playerOrder;
	private String serverIp;
	private String serverPort;
	private String backupServerIp;
	private String backupServerPort;
	private List<PlayerInfo> playerList = new ArrayList<PlayerInfo>();
	private MazeServer mazeGameServer;
	private MazeBackupServer mazeGameBackupServer;
	private MazeClient mazeGameClient;
	private String identity;
	private BackupService lookup_backup_service;
	private GameService lookup_primary_service;
	private BackupGameState backupGameState;
	private Registry registry = null;
	private int myOrder;
	
	public static final String MAZE_PLAYER = "MAZE_PLAYER";
	public static final String MAZE_PRIMARY_SERVER = "MAZE_PRIMARY_SERVER";
	public static final String MAZE_BACKUP_SERVER = "MAZE_BACKUP_SERVER";
	public static final String NOK = "NOK";
	
	
	public Game(String trackerIp, String playerIp, String trackerPort, String playerPort) {
		this.trackerIp = trackerIp;
		this.trackerPort = trackerPort;
		this.playerIp = playerIp;
		this.playerPort = playerPort;
	}
	
	public List<PlayerInfo> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<PlayerInfo> playerList) {
		this.playerList = playerList;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public Integer getGridN() {
		return gridN;
	}

	public void setGridN(Integer gridN) {
		this.gridN = gridN;
	}

	public Integer getTreasureK() {
		return treasureK;
	}

	public void setTreasureK(Integer treasureK) {
		this.treasureK = treasureK;
	}
	
	public void setNewPlayerOrder(String id,int order){
		for(PlayerInfo pInfo:playerList) {
			if(pInfo.getPlayerName().equals(playerId)) {
				pInfo.setPlayerOrder(order);
				break;
			}
		}
	}
	/*
	 * void -> void
	 * purpose. returns this Game status as one of
	 * - MAZE_PRIMARY_SERVER
	 * - MAZE_BACKUP_SERVER
	 * - MAZE_PLAYER
	 * 
	 * based on playerIP and order of joining
	 */
	public String getStatus() {
		String status = NOK;
		
		if(playerList.isEmpty()) {
			return status;
		} else {
			
			for(PlayerInfo pInfo:playerList) {
				if(pInfo.getPlayerName().equals(playerId) && playerOrder == 1) {
					status = MAZE_PRIMARY_SERVER;
				} else if(pInfo.getPlayerName().equals(playerId) && playerOrder == 2) {
					status = MAZE_BACKUP_SERVER;
				} else {
					status = MAZE_PLAYER;
				}
			}
		}
		
		return status;
	}
	
	public String getTrackerIp() {
		return trackerIp;
	}

	public void setTrackerIp(String trackerIp) {
		this.trackerIp = trackerIp;
	}

	public String getTrackerPort() {
		return trackerPort;
	}

	public void setTrackerPort(String trackerPort) {
		this.trackerPort = trackerPort;
	}

	public String getPlayerIp() {
		return playerIp;
	}

	public void setPlayerIp(String playerIp) {
		this.playerIp = playerIp;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public Integer getPlayerOrder() {
		return playerOrder;
	}

	public void setPlayerOrder(Integer playerOrder) {
		this.playerOrder = playerOrder;
	}

	public String getPlayerPort() {
		return playerPort;
	}

	public void setPlayerPort(String playerPort) {
		this.playerPort = playerPort;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getBackupServerIp() {
		return backupServerIp;
	}

	public void setBackupServerIp(String backupServerIp) {
		this.backupServerIp = backupServerIp;
	}

	public String getBackupServerPort() {
		return backupServerPort;
	}

	public void setBackupServerPort(String backupServerPort) {
		this.backupServerPort = backupServerPort;
	}

	public String figureOutServer(String whichServer) {
		String serverPlayer = null;
		
		if(whichServer.equals(Game.MAZE_PRIMARY_SERVER)) {
			serverPlayer = playerList.get(0).getPlayerName();
		} else if(whichServer.equals(Game.MAZE_BACKUP_SERVER)) {
			serverPlayer = playerList.get(1).getPlayerName();
		}
		
		return serverPlayer;
	}
	
	public ArrayList<String> getPlayerInfoById(String playerId){
		ArrayList<String> playerInfo = new ArrayList<String>();
		
		for(PlayerInfo pInfo:playerList) {
			if(pInfo.getPlayerName().equals(playerId)) {
				playerInfo.add(pInfo.getPlayerIP());
				playerInfo.add(pInfo.getPlayerPort());
				
				break;
			}
		}
		
		return playerInfo;
	}
	
	public Integer getMyOrder(String playerId) {
		Integer myOrder = 0;
		
		for(PlayerInfo pInfo:playerList) {
			if(pInfo.getPlayerName().equals(playerId)) {
				myOrder = pInfo.getPlayerOrder();
				break;
			}
		}
		
		return myOrder;
	}
	
	public void regenerateGameServer() {
		System.out.println("BackupServerIP is "+this.getPlayerIp());
		try {			
			lookup_backup_service = (BackupService) Naming.lookup("//"+this.getPlayerIp()+"/GameBackupService");
			backupGameState = lookup_backup_service.getGameState();
			System.out.println("Got the gamestate from backup server");
		} catch (MalformedURLException | RemoteException | NotBoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
		
		
		//TODO remove the dead entries of previous primary player from 
		if(backupGameState != null) {
			String[][] gameMap = backupGameState.getGameMap();
			Hashtable<String,SinglePlayerGameStates> playerStats = backupGameState.getPlayerStats();
			Map<String, Integer> playerAttendance = backupGameState.getPlayerAttendance();
		
			System.out.println("Unbind backup");
			//first unbind your self from backup services
			mazeGameBackupServer.unbindFromBackupService();
			GameState gs = backupGameState.getGameState();
			System.out.println("Regenerating Primary server");
			int removeDeadPrimary = 1;
			this.mazeGameServer = new MazeServer(trackerIp,playerIp, playerPort, gridN, treasureK, gameMap, playerStats, playerAttendance,gs,removeDeadPrimary);
			this.setIdentity(MAZE_PRIMARY_SERVER);;
			this.setServerIp(getPlayerIp());
			this.setServerPort(getPlayerPort());
			this.mazeGameClient.setLook_up("//"+this.getPlayerIp()+"/GameService");
			this.mazeGameClient.updateUI();						
			//update the tracker
			/*try {
				look_up.removeCrashedPlayer(playerList.get(0).getPlayerName());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
	}
	
	public void bindLookUps() {
		System.out.println("I am normal client, trying to bind to new primary");
		boolean bindSuccess = this.mazeGameClient.setLook_up("//"+this.getServerIp()+"/GameService");
		if(bindSuccess == true){
			int myPlayerOrder;
			try {
				myPlayerOrder = this.mazeGameClient.getPlayerOrder(this.getPlayerId());
				this.setNewPlayerOrder(this.getPlayerId(), myPlayerOrder);
				if(myPlayerOrder == 2 ){										
					this.appointNewBackupServer();
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		}
		//this.mazeGameClient.setLook_up_backup_service(this.getBackupServerIp());
	}
	
	/*This is for backup server crash*/
	public void regenerateBackUpServer(){
		try {
			
			System.out.println("Backup server crashed, so regenerating myself as new backupserver");
			registry = LocateRegistry.getRegistry();
			registry.unbind("GameBackupService");			
			this.mazeGameBackupServer = new MazeBackupServer(this.getPlayerIp(), this.getPlayerPort());			
			this.setBackupServerIp(this.getPlayerIp());
			this.setBackupServerPort(this.getBackupServerPort());
			this.setIdentity(MAZE_BACKUP_SERVER);
			//this.mazeGameClient.setLook_up_backup_service("//"+this.getBackupServerIp()+"/GameBackupService");
			//set the new backup server on the primary server so that it can update the game state	, update the gamestate once now	
			this.mazeGameClient.pingPrimary(this.getPlayerId());
			lookup_primary_service = (GameService) Naming.lookup("//"+this.getServerIp()+"/GameService");
			lookup_primary_service.setBackupService(this.getBackupServerIp());	
			//lookup_primary_service.updateBackupServer();
			this.mazeGameClient.pingPrimary(this.getPlayerId());
			System.out.println("Backup regeneration done");
			
		} catch (RemoteException | NotBoundException | MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*This is when primary crash and new backup chosen*/
	public void appointNewBackupServer() {	
		System.out.println("Appointing myself as new backupserver");
		this.mazeGameBackupServer = new MazeBackupServer(getPlayerIp(), getPlayerPort());
		
		this.setBackupServerIp(getPlayerIp());
		this.setBackupServerPort(getBackupServerPort());
		this.setIdentity(MAZE_BACKUP_SERVER);
		//this.mazeGameClient.setLook_up_backup_service("//"+this.getBackupServerIp()+"/GameBackupService");
		//set the new backup server on the primary server so that it can update the game state	, update the gamestate once now	
		try {
			lookup_primary_service = (GameService) Naming.lookup("//"+this.getServerIp()+"/GameService");
			lookup_primary_service.setBackupService(this.getBackupServerIp());	
			//lookup_primary_service.updateBackupServer();
			System.out.println("New backup server chosen");
		} catch (RemoteException | MalformedURLException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void removePrimaryServer() {
		
		playerList.remove(0);
		System.out.println("Removing the dead primary server and unbinding");
		try {
			registry = LocateRegistry.getRegistry();
			registry.unbind("GameService");
		} catch (RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!playerList.isEmpty()) {
			for(PlayerInfo pInfo: playerList) {
				pInfo.setPlayerOrder(pInfo.getPlayerOrder() - 1);
			}
		}
	}
	
	public void startCheckingOrder(){		
		
		Thread checkOrder = new Thread(){
			public void run(){	
				while(true){
					try {
						int currentOrder = getMyOrder(getPlayerId());
						int newOrder;
						newOrder = mazeGameClient.getPlayerOrder(getPlayerId());
						setNewPlayerOrder(getPlayerId(),newOrder);	
						
						if(newOrder != currentOrder && newOrder == 2 ){
							System.out.println("My New Order : "+ newOrder);
							System.out.println("My Current Order : "+ currentOrder);
							regenerateBackUpServer();
						}
						Thread.sleep(2000);
					} catch (RemoteException | InterruptedException e) {
						// TODO Auto-generated catch block	
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}						
			}
		};
		checkOrder.start();	
	}
	
	public static void main(String[] args)
			throws MalformedURLException, RemoteException, NotBoundException, UnknownHostException {

		if(args.length < 3 ) {
			System.err.println("One or more command line options missing");
			System.err.println("Usage:"+"\n"+"java Tracker <port> <grid-N> <treasure-K>");
			return;
		} else {
			String trackerIp = args[0];
			String port = args[1];
			String playerId = args[2];
			
			
			String playerIp = InetAddress.getLocalHost().getHostAddress();
			
			//Create the Game Instance
			Game mazeGame = new Game(trackerIp, playerIp, null, null);
			
			look_up = (TrackerService) Naming.lookup("//"+trackerIp+"/MazeGame");
			
			/*
			 * 1) contacts Tracker first to check if it can join the Game and obtain the existing player list
			 * TODO
			 */
			String joinStatus = look_up.joinMazeGame(playerIp, port, playerId);
			
			if(joinStatus.equals(Tracker.JOIN_SUCCESS)) {
				System.out.println("Successfully contacted Tracker");
				HashMap<String, Object> gameInfo = look_up.getMazeGameInfo();
				List<PlayerInfo> playerList = (ArrayList<PlayerInfo>) gameInfo.get(Tracker.PLAYER_LIST);
				
				/*for(PlayerInfo p: playerList) {
					System.out.println(p.getPlayerOrder());
				}*/
				
				
				Integer gridN = (Integer) gameInfo.get(Tracker.GRID_N);
				Integer treasureK = (Integer) gameInfo.get(Tracker.TREASURE_K);
				
				mazeGame.setPlayerList(playerList);
				mazeGame.setGridN(gridN);
				mazeGame.setTreasureK(treasureK);
				mazeGame.setPlayerIp(playerIp);
				mazeGame.setPlayerPort(port);
				mazeGame.setPlayerId(playerId);
				mazeGame.setTrackerIp(trackerIp);
				mazeGame.setTrackerPort(port);
				
				
				
				Integer myOrder = mazeGame.getMyOrder(playerId);
				System.out.println("Joining game my current order is: "+myOrder);
				mazeGame.setPlayerOrder(myOrder);
				/*2) After obtaining the player List it generates it self as either one of
				 *    - MAZE_PRIMARY_SERVER
				 *    - MAZE_BACKUP_SERVER
				 *    - MAZE_PLAYER
				 */

				mazeGame.setIdentity(mazeGame.getStatus());
				
				String identity = mazeGame.getIdentity();
				
				if(identity.equals(Game.MAZE_PRIMARY_SERVER)) {
					System.out.println("Initiating myself as primary");
					/*
					 * 3) If it generates itself as the primary server
					 *    - it initializes the gamestate (create gameMap allocate Trasures etc)
					 */
					mazeGame.mazeGameServer = new MazeServer(mazeGame.getTrackerIp(),mazeGame.getPlayerIp(), mazeGame.getPlayerPort(), mazeGame.getGridN(), mazeGame.getTreasureK(), null, null, null, null, 0);
					
					//for primary server the server IP / port will be same as playerIp / Port
					mazeGame.setServerIp(mazeGame.getPlayerIp());
					mazeGame.setServerPort(mazeGame.getPlayerPort());
					
					
					mazeGame.mazeGameClient = new MazeClient(playerId, mazeGame.getServerIp(), mazeGame.getServerPort(), mazeGame.getPlayerIp(), mazeGame.getPlayerPort(), mazeGame.getBackupServerIp(), mazeGame.getBackupServerPort(), mazeGame.getGridN(), Game.MAZE_PRIMARY_SERVER);
					
					int gameJoinStatus = mazeGame.mazeGameClient.joinMazeGame();
					
				} else if(identity.equals(Game.MAZE_BACKUP_SERVER)){
					System.out.println("Initiating myself as backup");
					mazeGame.mazeGameBackupServer = new MazeBackupServer(mazeGame.getPlayerIp(), mazeGame.getPlayerPort());

					//for backup server the server ip / port will be the 1st entry from the playerList
					String serverPlayer = mazeGame.figureOutServer(Game.MAZE_PRIMARY_SERVER);
					
					ArrayList<String> serverPlayerInfo = mazeGame.getPlayerInfoById(serverPlayer);
					mazeGame.setServerIp(serverPlayerInfo.get(0));
					mazeGame.setServerPort(serverPlayerInfo.get(1));
					
					mazeGame.mazeGameClient = new MazeClient(playerId, mazeGame.getServerIp(), mazeGame.getServerPort(), mazeGame.getPlayerIp(), mazeGame.getPlayerPort(), mazeGame.getBackupServerIp(), mazeGame.getBackupServerPort(), mazeGame.getGridN(), Game.MAZE_BACKUP_SERVER);
					
					int gameJoinStatus =  mazeGame.mazeGameClient.joinMazeGame();
					
				} else {
					
					System.out.println("Inside regular client");
					
					//TODO add the backup server capability
					
					//for regular players and backup server the server ip / port will be the 1st entry from the playerList
					String serverPlayer = mazeGame.figureOutServer(Game.MAZE_PRIMARY_SERVER);
					
					ArrayList<String> serverPlayerInfo = mazeGame.getPlayerInfoById(serverPlayer);
					mazeGame.setServerIp(serverPlayerInfo.get(0));
					mazeGame.setServerPort(serverPlayerInfo.get(1));
					
					String backupServerPlayer = mazeGame.figureOutServer(Game.MAZE_BACKUP_SERVER);
					
					ArrayList<String> backupServerPlayerInfo = mazeGame.getPlayerInfoById(backupServerPlayer);
					mazeGame.setBackupServerIp(backupServerPlayerInfo.get(0));
					mazeGame.setBackupServerPort(backupServerPlayerInfo.get(1));
					
					mazeGame.mazeGameClient = new MazeClient(playerId, mazeGame.getServerIp(), mazeGame.getServerPort(), mazeGame.getPlayerIp(), mazeGame.getPlayerPort(), mazeGame.getBackupServerIp(), mazeGame.getBackupServerPort(), mazeGame.getGridN(), Game.MAZE_PLAYER);
					
					
					int gameJoinStatus =  mazeGame.mazeGameClient.joinMazeGame();
					
					//if successfully joined Game
					if(1 == gameJoinStatus) {
						String[][] gameMap = mazeGame.mazeGameClient.getGameMap();
						
						if(gameMap == null) {
							System.out.println("Empty game map");
						}
						
						Hashtable<String, SinglePlayerGameStates> playerStats = mazeGame.mazeGameClient.getGameStatusInfo();
						
						//update the the backupserver - primary server will handle this
						//mazeGame.mazeGameClient.updateBackupServer(gameMap, playerStats);
					}
				}
				mazeGame.mazeGameClient.startPollingInputStream();
				mazeGame.startCheckingOrder();
				//Create a seprate thread which call for its presence to primary server befor attendance check routine 
				//continuosly mark attendance
				while(true) {
					
					try {
						
						//continuously ping primary (gets the player list with order info every time)
						mazeGame.mazeGameClient.pingPrimary(mazeGame.getPlayerId());
						int newOrder = mazeGame.mazeGameClient.getPlayerOrder(mazeGame.getPlayerId());
						mazeGame.setNewPlayerOrder(mazeGame.getPlayerId(),newOrder);
						/*
						int currentOrder = mazeGame.getMyOrder(mazeGame.getPlayerId());
						int newOrder = mazeGame.mazeGameClient.getPlayerOrder(mazeGame.getPlayerId());
						mazeGame.setNewPlayerOrder(mazeGame.getPlayerId(),newOrder);						 																			
						if(newOrder != currentOrder && newOrder == 2 ){								
							mazeGame.regenerateBackUpServer();;
						}	
						*/																		
						Thread.sleep(Game.waitToMarkAttendance);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NotBoundException e){
						
					} catch (RemoteException e) {
						mazeGame.setServerIp(null);
						mazeGame.setBackupServerIp(null);															
						myOrder = mazeGame.getMyOrder(mazeGame.getPlayerId());
						System.out.println("Inside ping primary exception, my order is: "+myOrder);
						if(2 == myOrder) {
							System.out.println("special case");
							// TODO Auto-generated catch block
							mazeGame.removePrimaryServer();   //reinitialize the player order
							
							
							if(!mazeGame.playerList.isEmpty()) {
								/*for(PlayerInfo pInfo: mazeGame.playerList) {
									
							
									if(pInfo.getPlayerOrder() == 1) {
										System.out.println("Regenating primary for "+ pInfo.getPlayerName());
										//regenerate itself as primary server
										mazeGame.regenerateGameServer();
										
									} else if(pInfo.getPlayerOrder() == 2) {
										System.out.println("Regenating backup for "+ pInfo.getPlayerName());
										//regenerate itself as Backup Server
										mazeGame.appointNewBackupServer();
									}
								}*/
								
								Integer myNewOrder = mazeGame.getMyOrder(mazeGame.getPlayerId());
								
								if(myNewOrder == 1) {
									System.out.println("Regenating primary for "+ mazeGame.getPlayerId()+"My New Order : "+myNewOrder);
									//regenerate itself as primary server
									mazeGame.regenerateGameServer();
								}
								/*
								if(myNewOrder == 2) {
									System.out.println("Regenating backup for "+ mazeGame.getPlayerId()+"My New Order : "+ myNewOrder);
									//regenerate itself as Backup Server
									mazeGame.appointNewBackupServer();
								}
								*/
								
							}
						} else {
							
							try {
								System.out.println("normal client");
								//Sleep for some time
								Thread.sleep(1500);
								
								//Contact Tracker to get the new player List
								HashMap<String, Object> rgameInfo = look_up.getMazeGameInfo();
								List<PlayerInfo> rplayerList = (ArrayList<PlayerInfo>) gameInfo.get(Tracker.PLAYER_LIST);
								
								//rplayerList contains the updated player List info after primary server crashed
								//get the ip of new primary and backup server and 
								mazeGame.setPlayerList(rplayerList);
								
								//for regular players and backup server the server ip / port will be the 1st entry from the playerList
								String serverPlayer = mazeGame.figureOutServer(Game.MAZE_PRIMARY_SERVER);
								
								ArrayList<String> serverPlayerInfo = mazeGame.getPlayerInfoById(serverPlayer);
								mazeGame.setServerIp(serverPlayerInfo.get(0));
								mazeGame.setServerPort(serverPlayerInfo.get(1));
								
								//This code is not needed, since now only the primary server will contact the backupServer
								/*String backupServerPlayer = mazeGame.figureOutServer(Game.MAZE_BACKUP_SERVER);
								
								ArrayList<String> backupServerPlayerInfo = mazeGame.getPlayerInfoById(backupServerPlayer);
								mazeGame.setBackupServerIp(backupServerPlayerInfo.get(0));
								mazeGame.setBackupServerPort(backupServerPlayerInfo.get(1));*/
								
								//finally bind look_ups and look_up_backup_service with the new primary and appoint itself as new backup server if its order is 2.
								mazeGame.bindLookUps();
								
								
								
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						
						
					}
				}
				

				
			} else {
				
				System.out.println(joinStatus);
				//TODO call the client Swing Dialog box with appropriate error message
			}
						
		}
	}
}
