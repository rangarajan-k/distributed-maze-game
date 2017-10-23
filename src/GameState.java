import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.LinkedList;

public class GameState implements Serializable, Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int[][] treasureXY;
	public Hashtable<String,Integer> playerScore;
	public Hashtable<String,int[]> playerXY;
	LinkedList<String> pList = new LinkedList<String>();


	GameState(int k){
		this.treasureXY  =  new int[k][2];
		this.playerScore = new Hashtable<String,Integer>();
		this.playerXY = new Hashtable<String,int[]>();
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
//	method to add a playerScore entry
	public Integer addPlayerScore(String id, int score){
		try{
//			return this.playerScore.put(id, score);
			Integer addedScore = this.playerScore.put(id, score);
//			System.out.print("GameState addPlayerScore: ");
//			System.out.println(this.playerScore.get(id));
			return addedScore;
		}
		catch(NullPointerException e){
			System.out.print("GameState addPlayerScore error: ");
			System.out.println(e.getMessage());
			return (Integer) null;
		}
//		return 1;

	}
//	method to drop a playerScore entry
	public int dropPlayerScore(String id){
		try{
			System.out.print("GameState dropPlayerScore : ");
			System.out.println(this.playerScore.get(id));
			return this.playerScore.remove(id);
		}
		catch(Exception e){
			
			System.out.println(e.getMessage());
			return (Integer) null;
		}

	}
//	if call getPlayerScore without id, will return the whole hashtable
	public Hashtable<String, Integer> getPlayerScore(){
		return this.playerScore;
	}

//	if call getPlayerScore with id, will return the score of the id
	public int getPlayerScore(String id){
		return this.playerScore.get(id);
	}
	
	public int putPlayerXY(String id, int x, int y){
		int[] newPos = {x,y};
		this.playerXY.put(id, newPos);
		return 1;
	}
	public int dropPlayerXY(String id){
		this.playerXY.remove(id);
		return 1;
	}
	

	public int getPlayerOrder(String id) throws RemoteException{
		int order = this.pList.indexOf(id)+1;
		return order;
	}
	
	public LinkedList<String> getPlayerOrderList() throws RemoteException{
		return this.pList;
	}
	
}
