import java.io.Serializable;


public class SinglePlayerGameStates implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String id;
	public String ip;
	public String portNumber;
	public int xCoord;
	public int yCoord;
	public int score = 0;
	public SinglePlayerGameStates(String id, String ip, String portNumber) {
		this.id = id;
		this.ip = ip;
		this.portNumber =portNumber;
//		this.xCoord = xCoord;
//		this.xCoord = xCoord;
//		this.id = id;
	}
	
	public synchronized void setId(String Id) {
		this.id = Id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public synchronized void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getIp() {
		return this.ip;
	}
	
//	public synchronized void setPort(String portNumber) {
//		this.portNumber = portNumber;
//	}
//	
//	public String getPort() {
//		return this.portNumber;
//	}
	
	public synchronized void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}
	
	public String getPortNumber() {
		return this.portNumber;
	}
	
	public synchronized void setXCoord(int xCoord) {
		this.xCoord = xCoord;
	}
	public int getXCoord() {
		return this.xCoord;
	}

	public synchronized void setYCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	public int getYCoord() {
		return this.yCoord;
	}

	public synchronized void setScore(int score) {
		this.score = score;
	}
	
	public synchronized void increaseScore(int score) {
		this.score = this.score+score;
	}
	
	public int getScore() {
		return this.score;
	}
	
}

