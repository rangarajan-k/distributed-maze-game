

import java.util.Random;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
//import java.util.HashMap;
import java.util.Hashtable;
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
	GameState gs;
	Hashtable<String, SinglePlayerGameStates> playerStats =
            new Hashtable<String, SinglePlayerGameStates>();  // playerStats table with key(player ID) and player object
	Random rand = new Random();


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
	
//	object constructor
	GameEngine(int n, int k){
		this.rand.setSeed(1);
		this.n = n;
		this.k = k;
		this.gs = new GameState(k);
//		this.treasureXY = new int[k][2];
//		Array.fill(this.treasureXY,-1);
		for (int i =0; i<k;i++){
			gs.treasureXY[i][0]=-1;
			gs.treasureXY[i][1]=-1;
		}
		
//		treasureXY.
		mapGen();
		treasureGen(this.k);

//		rand.ints()
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
	public synchronized int addNewPlayer(SinglePlayerGameStates p){
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
	public synchronized int	dropPlayer(String playerKey){
		// check amount of players first
		if(this.playerStats.get(playerKey) == null) {
			System.out.println("player not exist");
			return 0;
		}
		try{
			SinglePlayerGameStates p = this.playerStats.get(playerKey);
			this.gameMap[p.getYCoord()][p.getXCoord()] = null; // set the map of the player occupying to null
			this.playerStats.remove(playerKey); // drop the player with key value
			this.gs.dropPlayerScore(playerKey);
			System.out.println(this.gs.playerScore.keySet().toString());
			this.gs.dropPlayerXY(playerKey);
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
	private synchronized int moveLogic(SinglePlayerGameStates p,String move){
		int[] newPos = new int[2]; // newPos[0] is x position, newPos[1] is y position
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
        	break;
        case "9":
        	this.dropPlayer(p.getId());
        	break;
        default:
        	System.err.println("unknow case sent to the move method.");
        	break;
		}

		if(newPos[0]>=0 && newPos[1]>=0 && newPos[0]<this.n && newPos[1]<this.n){ //if not hitting boundary
			if (this.gameMap[newPos[1]][newPos[0]] ==null){ //if go to a empty position while no treasure
				this.gameMap[p.getYCoord()][p.getXCoord()] = null; //set the gameMap of the current position to null as it is empty after move
				p.setXCoord(newPos[0]);
				p.setYCoord(newPos[1]); //update player's position
				this.gameMap[p.getYCoord()][p.getXCoord()] = p.getId(); //put new position of player on map
//				System.out.println("move to null");
				return 1;
			}
			else if(this.gameMap[newPos[1]][newPos[0]] == "*"){ //if go to a position with treasure
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
				return 2;
			}
			else {  //hit other players
//				System.out.println("hit other players");
				return 3;
			}
		}
		else {
//			System.out.println("hit boundary");
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
	
	public String[][] getGameMap() throws RemoteException {
		// TODO Auto-generated method stub
		return this.gameMap;
	}
	
	@Override
//	public String addPlayer(String id, String ip, String portNumber ){
//		try{
//			SinglePlayerGameStates p = new SinglePlayerGameStates(id,ip,portNumber) ;
//			this.addNewPlayer(p);
//			return "1";
//			}
//		catch ( Exception e){
//			return e.getMessage();
//		}
//	}
	public synchronized SinglePlayerGameStates addPlayer(String id, String ip, String portNumber ){
		try{
			SinglePlayerGameStates p = new SinglePlayerGameStates(id,ip,portNumber) ;
			this.addNewPlayer(p);
			gs.addPlayerScore(id, 0);
//			gs.playerScore.put(id, 0);
			System.out.print("added game state " + id + ": ");
			System.out.println(gs.playerScore.get(id));
			return p;
			}
		catch ( Exception e){
			return null;
		}
	}
	
	public GameState getGameState() throws RemoteException{
		return this.gs;
	}
}
