/*
 * Tracker acts as a point of contact to players only on two occasions
 *  
 * 1) when a new player wants to join the MazeGame
 * 
 * 2) when there is a player crash
 * 
 * Tracker Resposibility
 * 
 * 1) Accept new player join request
 * 
 * 2) Always maintain the active player list 
 * 
 * 3) Accept player crash info and update the active player list
 *
 * 4) Accept existing player exit info and update the active player list
 */

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import edu.nus.mazegame.trackerservice.TrackerService;

public class Tracker implements TrackerService {
	
	public static final String GAME_OVERLOAD = "GAME_OVERLOAD";
	
	public static final String PLAYER_ALREADY_EXISTS = "PLAYER_ALREADY_EXISTS";
	
	public static final String JOIN_SUCCESS = "JOIN_SUCCESS";
	
	public static final String PLAYER_LIST = "PLAYER_LIST";
	
	public static final String GRID_N = "GRID_N";
	
	public static final String TREASURE_K = "TREASURE_K";
	
	private int putJoinDelay = 0;
	
	//tracker ip address
	private String trackerIP;
	
	//tracker port
	private String trackerPort;
	
	//mazegame grid dimension
	private Integer gridN;   //maximum locations is gridN * gridN
	
	//mazegame treasure
	private Integer treasureK;
	
	//maxmum player allowed to join the game [ (gridN * gridN) - treasureK ]
	private Integer maxAllowedPlayer;
	
	//current player count
	private static Integer curPlayerCount = 0;
				
	private ArrayList<PlayerInfo> playerList = new ArrayList<PlayerInfo>();

	public String getTrackerIP() {
		return trackerIP;
	}

	public void setTrackerIP(String trackerIP) {
		this.trackerIP = trackerIP;
	}

	public String getTrackerPort() {
		return trackerPort;
	}

	public void setTrackerPort(String trackerPort) {
		this.trackerPort = trackerPort;
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

	public Integer getMaxAllowedPlayer() {
		return maxAllowedPlayer;
	}

	public void setMaxAllowedPlayer() {
		this.maxAllowedPlayer = (gridN * gridN) - treasureK;
	}

	protected Tracker() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String helloTo(String name) throws RemoteException {
		System.err.println(name + " is trying to contact!");
        return "Server says hello to " + name;
	}
	
	/*
	 * purpose. returns the order of joining for new player
	 */
	private Integer  getPlayerOrder() {
		Integer playerOrder = 0;
		
		if(this.playerList.isEmpty()) {
			playerOrder = 1;
		} else {
			int pSize = this.playerList.size() - 1;
			PlayerInfo p = (PlayerInfo) this.playerList.get(pSize);
		
			playerOrder = p.getPlayerOrder() + 1;
		}
		
		return playerOrder;
	}

	/*
	 * returns player info base on the given player name
	 */
	public PlayerInfo getPlayerInfo(String playerName) {
		PlayerInfo plyrInfo = new PlayerInfo();
		
		for(PlayerInfo pInfo: playerList) {
			if(pInfo.getPlayerName() == playerName) {
				plyrInfo.setPlayerIP(pInfo.getPlayerIP());
				plyrInfo.setPlayerPort(pInfo.getPlayerPort());
				plyrInfo.setPlayerName(pInfo.getPlayerName());
				plyrInfo.setPlayerOrder(pInfo.getPlayerOrder());
			}
		}
		
		return plyrInfo;
	}
	
	public boolean playerExists(String playerName) {
		boolean playerFound = false;
		
		for(PlayerInfo pInfo : playerList) {
			
			if(pInfo.getPlayerName().equals(playerName)) {
				playerFound = true;
				break;
			}
		}
		
		return playerFound;
	}
	
	/*
	 * String -> String
	 * purpose.
	 * returns one of
	 * JOIN_SUCCESS if new player can join game successfully
	 * GAME_OVERLOAD if the game is already full
	 * PLAYER_ALRREADY_EXISTS if the new player is using the existing player name
	 */
	public String canJoinGame(String newPlayerName) {
		String joinStatus = JOIN_SUCCESS;
		
		if(curPlayerCount < maxAllowedPlayer) {
			if(playerExists(newPlayerName)) {
				joinStatus = PLAYER_ALREADY_EXISTS;
			}
		} else {
			joinStatus =  GAME_OVERLOAD;
		}
		
		return joinStatus;
	}
	
	@Override
	public HashMap<String, Object> getMazeGameInfo() throws RemoteException{
		HashMap<String, Object> gameInfo = new HashMap<String, Object>();
		
		gameInfo.put(PLAYER_LIST, this.playerList);
		gameInfo.put(GRID_N, this.gridN);
		gameInfo.put(TREASURE_K, this.treasureK);
		
		return gameInfo;
		
	}
	
	public void initFirstPlayerPing() {
		
		System.out.println("Inside First player Ping : "+ playerList.get(0).getPlayerIP());

		Thread firstPlayerPing = new Thread("Pinging First Player to know if atleast one player is there in the game") {
			public void run() {
				
				int attemptCount = 0;
				
				while(true) {
					if(!playerList.isEmpty()) {
						String firstPlayerIp = playerList.get(0).getPlayerIP();
						//System.out.println("Currently pinging player : "+ playerList.get(0).getPlayerName());
						
						try {
							GameService look_up_first_player = (GameService) Naming.lookup("//"+firstPlayerIp+"/GameService");
							
							boolean isFirstPlayerReachable = look_up_first_player.iAmAlive();
							
							if(isFirstPlayerReachable) {
								//System.out.println("Yes the first player is reachable");
								attemptCount = 0;
							}
						} catch (MalformedURLException | NotBoundException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						} catch(RemoteException e) {
							System.out.println("No the first player is not reachable");
							if(2 == attemptCount) {
								
								//reset the attempt count
								attemptCount = 0;
								
								//clear the playerList as no player exists in the game
								playerList.clear();
								
								//unbind GameService from RMI as the Game will restart
								try {
									Registry registry = LocateRegistry.getRegistry();
									registry.unbind("GameService");
								} catch (RemoteException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (NotBoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								System.out.println("Clearing the Tracker player list");
							} else {
								attemptCount += 1;
							}
						}
					}
					
					try {
						Thread.sleep(500); //sleep for 0.5sec
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
		};
		
		firstPlayerPing.start();
	}
	
	@Override
	public String joinMazeGame(String newPlayerIp, String newPlayerPort, String newPlayerName) throws RemoteException {
		String joinStatus = canJoinGame(newPlayerName);
		try {
			if(putJoinDelay == 1) {
			Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(joinStatus.equals(JOIN_SUCCESS)) {
			PlayerInfo newPlayer = new PlayerInfo();
			
			newPlayer.setPlayerIP(newPlayerIp);
			newPlayer.setPlayerPort(newPlayerPort);
			newPlayer.setPlayerName(newPlayerName);
			newPlayer.setPlayerOrder(getPlayerOrder());
			
			playerList.add(newPlayer);
			curPlayerCount += 1;
			
			//start a new thread only for 1st player
			//where, Tracker will continuosly always ping the 1st player
			if(1 == curPlayerCount) {
				initFirstPlayerPing();
			}
			
		}
		
		return joinStatus;
	}
	
	@Override
	public boolean removeCrashedPlayer(String playerName) throws RemoteException{
		boolean status = false;
		int playerIndex = -1;
		
		//System.err.println("In remove player from tracker");
		//check if the crashed player exists in the active player list
		if(playerExists(playerName)) {
			
			//remove the player
			//Iterator<PlayerInfo> iter = playerList.iterator();
			int changeFurtherOrders = 0;
			//while(iter.hasNext()) {
			
			for(int i = 0; i < this.playerList.size(); i++) {
				PlayerInfo pInfo = (PlayerInfo) this.playerList.get(i);
				
				if(pInfo.getPlayerName().equals(playerName)) {
					playerIndex = i;
					System.out.println("Removing Player Index "+playerIndex);
					putJoinDelay = 1;
					//playerList.remove(pInfo);
					curPlayerCount -= 1;
					changeFurtherOrders = 1;
					continue;
				}
				if(changeFurtherOrders == 1){
					
					int currentOrder = pInfo.getPlayerOrder();
					pInfo.setPlayerOrder(currentOrder-1);
					System.out.println("Changing order " + currentOrder + "to new order: "+ (currentOrder-1));
				}
			}
			
			if(playerIndex != -1) {
				this.playerList.remove(playerIndex);
			}
//			//PlayerInfo pInfo = getPlayerInfo(playerName);
//			
//			//playerList.remove(pInfo);
//			System.err.println("Length "+getPlayerInfo(playerName).getPlayerIP());
			status = true;
		}
		
		return status;
	}
	
	public void initRMI() {
		// set ip address of rmi server
	    System.setProperty("java.rmi.server.hostname", trackerIP);
	    System.setProperty("java.rmi.activation.port", trackerPort.toString());
	    
	    
//	    //try to register RMI server on specific port 
//	    try
//	    {
//	        LocateRegistry.createRegistry();
//	    }
//	    catch (Exception e)
//	    {
//	        System.err.println(e.getMessage());
//	    }
	}
	
	public void startRMI(String trackerURL) {
		System.err.print("starting RMI server ...");
		
		TrackerService stub = null;
		Registry registry = null;
		
		try
	    {
			stub = (TrackerService) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.getRegistry();
			registry.bind("MazeGame", stub);
	        
	    } catch (Exception e) {
	        System.out.println("error: could not initialize master control RMI server");
	        System.exit(1);
	    }
	}
	
	public static void main(String[] args){

        try {
        	
        	if(args.length < 2 ) {
    			System.err.println("One or more command line options missing");
    			System.err.println("Usage:"+"\n"+"java Tracker <port> <grid-N> <treasure-K>");
    			return;
    		} else {
    			/*
    			System.err.println("0:"+args[0]);
    			System.err.println("1:"+args[1]);
    			System.err.println("2:"+args[2]);
    			//1System.err.println("3:"+args[3]);
    			System.err.println(args.length);
    			
    			System.exit(0);
 				*/
    			Tracker mazeGameTracker = new Tracker();
    			
    			String trackerIp = InetAddress.getLocalHost().getHostAddress();
    			System.out.println("My IP is " + trackerIp);
    			String port = args[0];
    			Integer gridN = Integer.parseInt(args[1]);
    			Integer treasureK = Integer.parseInt(args[2]);
    			String trackerUrl = "//"+trackerIp+"/MazeGame";
    			
    			mazeGameTracker.setTrackerIP(trackerIp);
    			mazeGameTracker.setTrackerPort(port);
    			mazeGameTracker.setGridN(gridN);
    			mazeGameTracker.setTreasureK(treasureK);
    			mazeGameTracker.setMaxAllowedPlayer();
    			
    			//initialize the RMI setup
    			//mazeGameTracker.initRMI();
    			
    			//start the RMI server
    			mazeGameTracker.startRMI(trackerUrl);
    			
                System.err.println("Server ready");
    		}
        } catch (Exception e) {

            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();

        }

    }

}
