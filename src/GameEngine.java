

import java.util.Random;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
//import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
//import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
//game state information maintained inside server
public class GameEngine implements GameService{

	private static final long serialVersionUID = 1L;
//private static final long serialVersionUID = 1L;


	//	Pair<String, Player> playerIdList = new Pair<String, Player>();
	String[][] gameMap;
//	int[][] treasureXY;
	int n; //maze length
	int k; //amount of treasures
	int playerAmount = 0;
	Map<String, Integer> playerAttendance = new HashMap<String, Integer>();
	Integer attendanceRoutineCheck = 2000; //2seconds
	GameState gs;
	Hashtable<String, SinglePlayerGameStates> playerStats =
            new Hashtable<String, SinglePlayerGameStates>();  // playerStats table with key(player ID) and player object
	Random rand = new Random();
	private BackupService lookup_backup_service;
	private static TrackerService look_up_tracker;
	
//	LinkedList<String> pList = new LinkedList<String>();


//	generate k treasured inside the maze
	private void treasureGen(int k){
		int i = 0;
		while  (i<k ){
//			System.out.println(i);
			int[] randIdx = this.rand.ints(2,0,this.n).toArray();  // generate 2 random numbers for treasure allocation
//			System.out.println(randIdx.toString());
			if (this.gameMap[randIdx[0]][randIdx[1]] == null) { // if the generated position is not a treasure, set it as a treasure
					this.gameMap[randIdx[0]][randIdx[1]] = "*";
					i+=1; // only increase the i while the place is not a treasure, to make sure k treasure has been placed
					updateTreasureXY();
				}
			}
	}
// generate a map with empty strings
	private void mapGen(){
			this.gameMap = new String[this.n][this.n];

	}
	
	public void initiatePlayerAttendance() {
		Thread attendance = new Thread("Player Attendannce") {
			public void run() {
				
				//It will run continuosly
				while(true) {
					//check the attendance if all the players have marked their presence and reset it.
					//if not, remove record of that player
					Iterator attendance = playerAttendance.entrySet().iterator();
					ArrayList<String> toRemove = new ArrayList<String>();
					
					while(attendance.hasNext()) {
						Map.Entry<String, Integer> attn = (Map.Entry<String, Integer>) attendance.next();
						String playerKey = attn.getKey();
						Integer playerattn = attn.getValue();
						
						if(playerattn == 0) {
							//player failed to mark the attendance or crashed remove the dead player
							System.out.println("Player key: "+playerKey+" attendance: "+playerattn);
							dropPlayer(playerKey);
							toRemove.add(playerKey);							
						} else {
							//simply reset the attendance
							playerAttendance.replace(playerKey, 0);
						}
					}
					
					//remove all dropped players fromt the main playerattendance list
					if(toRemove.size() > 0){
						for(int i=0; i < toRemove.size(); i++){
							String pid = toRemove.get(i);
							playerAttendance.remove(pid);
						}
					}
					updateBackupServer();
					//sleep and wakeup after attendandence routine check
					try {
						Thread.sleep(attendanceRoutineCheck);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		attendance.start();
	}
	
	public void markAllAttendanceToOne() {
		Iterator attendance = playerAttendance.entrySet().iterator();
		ArrayList<String> toRemove = new ArrayList<String>();
		
		while(attendance.hasNext()) {
			Map.Entry<String, Integer> attn = (Map.Entry<String, Integer>) attendance.next();
			String playerKey = attn.getKey();
				//simply reset the attendance
				playerAttendance.put(playerKey, 1);
			
		}
	}
	
//	object constructor
	public GameEngine(String trackerIp,int n, int k, String[][] gameMap, Hashtable<String, SinglePlayerGameStates> playerStats, Map<String, Integer> playerAttendance,GameState newgs, int removeDeadPrimary){
		this.rand.setSeed(1);
		this.n = n;
		this.k = k;
		
		System.out.println("Firing primary game engine");
//		this.treasureXY = new int[k][2];
//		Array.fill(this.treasureXY,-1);
		try {
			look_up_tracker = (TrackerService) Naming.lookup("//"+trackerIp+"/MazeGame");
		} catch (MalformedURLException | RemoteException | NotBoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(gameMap != null && playerStats != null && playerAttendance != null) {
			System.out.println("New primary, setting the gamestate got from backup");
			this.gameMap = gameMap;
			this.playerStats = playerStats;
			this.playerAttendance = playerAttendance;
			markAllAttendanceToOne();
			/*for(int x=0;x < this.gameMap.length; x++){
				for(int y=0;y < this.gameMap.length; y++){
					System.out.print(this.gameMap[x][y]+"\t");
				}
				System.out.println("");
			}
			*/
			
			if(removeDeadPrimary == 1){
				try {
					this.gs = (GameState) newgs.clone();					
					String primaryId = this.gs.getPlayerOrderList().get(0);
					this.dropPlayer(primaryId);
				} catch (RemoteException | CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		else{
			this.gs = new GameState(k);
	//		treasureXY.
			for (int i =0; i<k;i++){
				this.gs.treasureXY[i][0]=-1;
				this.gs.treasureXY[i][1]=-1;
			}
			mapGen();
			treasureGen(this.k);
		}
				
//		rand.ints()
		//initiate the player attedance sheet in a separate independent thread
		initiatePlayerAttendance();
	}
	public int updateTreasureXY() {
		int treasureI = 0;
		for (int i = 0; i<this.n; i++){
			for (int j = 0; j<this.n; j++){
				if(this.gameMap[i][j] == "*"){
					gs.treasureXY[treasureI][0] = j; //col number is x
					gs.treasureXY[treasureI][1] = i; //col number is x
					treasureI += 1;
				}
			}
		}
//		System.out.println("treasureXY updated to: ");
//		for (int i = 0; i<this.k; i++){
//			System.out.print(gs.treasureXY[i][0]);
//			System.out.print(" , ");
//			System.out.print(gs.treasureXY[i][1]);
//			System.out.println();		
//		}
		return 1;
	}
	
	// return 1 if player added successfully
	// return -1 if player added unsuccessfully while not cause by map is full
	// return 0 if player added unsuccessfully because of map is full
	public int addNewPlayer(SinglePlayerGameStates p){
		// check amount of players first
		if(this.playerStats.size()>=n*n-k) {
			System.out.println("too many players");
			return 0;
		}
//		System.out.println(playerStats.size());
//		System.out.println(n*n);
		// if map is not full, start to find a place and add player
		try{
			boolean addNotDone = true;
			while(addNotDone)
			{
				int[] randIdx = this.rand.ints(2,0,this.n).toArray();  // generate 2 random numbers for treasure allocation

				if (this.gameMap[randIdx[0]][randIdx[1]] == null) { // if the generated position is not a treasure, set it as a treasure
						this.gameMap[randIdx[0]][randIdx[1]] = p.getId();
						p.setXCoord(randIdx[1]); // 2nd value is column number or x value
						p.setYCoord(randIdx[0]); // 1st value is row number or y value
						gs.putPlayerXY(p.getId(), randIdx[1], randIdx[0]);
						System.out.println("playerXY keys are");
						System.out.println(gs.playerXY.keySet().toString());
						addNotDone = false;
					}
			}
			this.playerStats.put(p.getId(), p); // add player into the hashTable
			this.playerAttendance.put(p.getId(), 1); //add this player into attendance sheet, when added player is present
			if(this.gs.pList.size() > 1){
				updateBackupServer();
			}
			return 1;
		}

		catch( Exception e){
			System.out.println(e.getMessage());
			return -1;
		}

	}

	// return 1 if player dropped successful, -1 if player drop unsuccessfully while not cause by player not exist
	//  0 if player not exist
	@Override
	public int dropPlayer(String playerKey){
		// check amount of players first
		if(this.playerStats.get(playerKey) == null) {
			System.out.println("Dropping player,player not exist");
			return 0;
		}
		try{
			int playerOrder = this.gs.getPlayerOrder(playerKey);
			SinglePlayerGameStates p = this.playerStats.get(playerKey);
			this.gameMap[p.getYCoord()][p.getXCoord()] = null; // set the map of the player occupying to null
			this.playerStats.remove(playerKey); // drop the player with key value
			this.gs.dropPlayerScore(playerKey);
			System.out.println(this.gs.playerScore.keySet().toString());
			this.gs.dropPlayerXY(playerKey);
			this.gs.pList.remove(playerKey);
			 //updatebackup at all times except primary regeneration
			look_up_tracker.removeCrashedPlayer(playerKey);
			if(playerOrder != 1){
				updateBackupServer();
			}
			return 1;
		}
		catch( Exception e){
			System.out.println(e.getMessage());
			return -1;
		}

	}

	// return 1 if player moved successful, no score added
	// return 2 if player moved successful, score added
	// return 3 if player move hit other players
	// return 4 if player hit boundary
	// return -1 when error happen
	//  0 if player not exist
	@Override
	public int movePlayer(String playerKey,String move){
		if(this.playerStats.get(playerKey) == null) {
			System.out.println("player not exist");
			return 0;
		}
		try{
			SinglePlayerGameStates p  = this.playerStats.get(playerKey);
			this.gs.putPlayerXY(p.getId(), p.getXCoord(), p.getYCoord());
			return moveLogic(p,move);
		}
		catch( Exception e){
			return -1;
		}		

	}
//	private void moveDecision(Player p, int[] newXY){
//
//	}
	private int moveLogic(SinglePlayerGameStates p,String move){
		int[] newPos = new int[2]; // newPos[0] is x position, newPos[1] is y position
//		newPos[0] = -1;
//		newPos[1] = -1;
		switch (move) {   //case 1,2,3,4: up/north, down/south, left/west, right/east
        case "4":
        	newPos[0] = p.getXCoord();
        	newPos[1] = p.getYCoord() - 1; //move up/north
        	break;
        case "2":
        	newPos[0] = p.getXCoord();
        	newPos[1] = p.getYCoord() + 1; //move down/south
            break;
        case "1":
        	newPos[0] = p.getXCoord() - 1; //move Left/west
        	newPos[1] = p.getYCoord();
        	break;
        case "3":
        	newPos[0] = p.getXCoord() + 1; //move right/east
        	newPos[1] = p.getYCoord();
        	break;
        case "0":
        	//return the localState        	
        	return 5;
		case "9":
        	this.dropPlayer(p.getId());
        	break;
        default:
        	System.err.println("unknow case sent to the move method.");
        	break;
		}
		//System.out.println("New pos of player is "+newPos[1]+","+newPos[0]);
		//System.out.println("Existing tile in that position is "+this.gameMap[newPos[1]][newPos[0]]);
		if(newPos[0]>=0 && newPos[1]>=0 && newPos[0]<this.n && newPos[1]<this.n){ //if not hitting boundary
			// new position is a empty space
			if (this.gameMap[newPos[1]][newPos[0]] ==null){ //if go to a empty position while no treasure
				this.gameMap[p.getYCoord()][p.getXCoord()] = null; //set the gameMap of the current position to null as it is empty after move
				p.setXCoord(newPos[0]);
				p.setYCoord(newPos[1]); //update player's position
				this.gameMap[p.getYCoord()][p.getXCoord()] = p.getId(); //put new position of player on map
//				System.out.println("move to null");
				updateBackupServer();
				return 1;
			}
			// new position is a treasure
			else if(this.gameMap[newPos[1]][newPos[0]].equals("*")){ //if go to a position with treasure
				this.gameMap[p.getYCoord()][p.getXCoord()] = null; //set the gameMap of the current position to null as it is empty after move
				p.setXCoord(newPos[0]);
				p.setYCoord(newPos[1]); //update player's position
				this.gameMap[p.getYCoord()][p.getXCoord()] = p.getId(); //put new position of player on map
				p.increaseScore(1);
				this.gs.addPlayerScore(p.getId(),p.getScore());
//				System.out.print("current player score is: " );
//				System.out.println(p.getScore());
				this.treasureGen(1); //after score, need to add one treasure into the map
//				System.out.println("move to treasure");
				updateBackupServer();
				return 2;
			}
			// new position is other player
			else {  //hit other players
//				System.out.println("hit other players");
				updateBackupServer();
				return 3;
			}
		}
		else {
//			System.out.println("hit boundary");
			updateBackupServer();
			return 4; //hit boundary
		}
	}
	public void printMap(){
//		System.out.println(Array.getLength(this.gameMap));
		System.out.println("----------------------------");
		for (int i=0; i<Array.getLength(this.gameMap);i++)
		{
			System.out.println(Arrays.deepToString(this.gameMap[i]));
		}

	}

	public Hashtable<String, SinglePlayerGameStates> getGameStatus() throws RemoteException {
		// TODO Auto-generated method stub
		return this.playerStats;
	}
	
	//this is for backupserver
	public void setGameStatus(Hashtable<String, SinglePlayerGameStates> playerStats) throws RemoteException{
		this.playerStats = playerStats;
	}
		
	public String[][] getGameMap() throws RemoteException {
		// TODO Auto-generated method stub
		return this.gameMap;
	}
	
	@Override
	public SinglePlayerGameStates addPlayer(String id, String ip, String portNumber ){
		try{
			System.out.println ("Adding player to game :"+id);
			SinglePlayerGameStates p = new SinglePlayerGameStates(id,ip,portNumber) ;
			this.addNewPlayer(p);
			gs.addPlayerScore(id, 0);			
			this.gs.pList.add(id);
			//if the added player is the second player initiate rmibind with that client
			if(this.gs.pList.size() == 2){
				this.setBackupService(ip);
			}
//			gs.playerScore.put(id, 0);
			System.out.print("added game state " + id + ": ");
			System.out.println(gs.playerScore.get(id));
			return p;
			}
		catch ( Exception e){
			return null;
		}
	}
	
	@Override
	public void setBackupService(String ip){
		System.out.println("Setting a new backup server");
		try {
			lookup_backup_service = (BackupService) Naming.lookup("//"+ip+"/GameBackupService");
			//manually put a ping for this player in player attendance
			String backupId = this.gs.pList.get(1);
			this.playerAttendance.put(backupId, 1);
			updateBackupServer();
			this.playerAttendance.put(backupId, 1);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	@Override
	public void updateBackupServer() {
		//updatebackupserver only if player count >= 2
		if(this.gs.pList.size() >= 2){
			try {
				//System.out.println("Updating Backup from primary");			
				lookup_backup_service.updateGameState(this.getGameStatus(),this.getGameMap(),this.gs);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	@Override
	public void setGameMap(String[][] gameMap) throws RemoteException {
		
		System.out.println("Player Updating Move to Backup server");
		// TODO Auto-generated method stub
		this.gameMap = gameMap;
	}
	
	@Override
	public void updatePresense(String playerKey) throws RemoteException{
		// TODO Auto-generated method stub
		//mark your attendance
		//System.out.println("Attendance of Player : "+ playerKey);
		playerAttendance.put(playerKey, 1);
	}
	public GameState getGameState() throws RemoteException{
		return this.gs;
	}
	
	public SinglePlayerGameStates getPlayerState(String id) throws RemoteException{
		return this.playerStats.get(id);
	}
	
	public int getPlayerOrder(String id) throws RemoteException{
		int order = (this.gs.pList.indexOf(id)) +1;
		return order;
	}
	
	public LinkedList<String> getPlayerOrderList() throws RemoteException{
		return this.gs.pList;
	}
	
	public void removePrimaryServer(){
		String priSerId = this.gs.pList.getFirst();
		this.dropPlayer(priSerId);
		System.out.println(gs.pList);
	}
	
	@Override
	public boolean iAmAlive() throws RemoteException {
		// TODO Auto-generated method stub
		return true;
	}
}
