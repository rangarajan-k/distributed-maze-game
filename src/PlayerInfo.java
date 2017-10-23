import java.io.Serializable;

/*
 * PlayerInfo is maintains the information of connecting players 
 */
//package edu.nus.mazegame.trackerservice;

public class PlayerInfo implements Serializable, Comparable<PlayerInfo>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//connecting player ip address
	private String playerIP;
	
	//connecting player port 
	private String playerPort;
	
	//connecting player two charachter name
	private String playerName;
	
	//Order of player joinings
	private Integer playerOrder;
	
	
	public String getPlayerIP() {
		return playerIP;
	}
	
	public void setPlayerIP(String playerIP) {
		this.playerIP = playerIP;
	}
	
	public String getPlayerPort() {
		return playerPort;
	}
	
	public void setPlayerPort(String playerPort) {
		this.playerPort = playerPort;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public Integer getPlayerOrder() {
		return playerOrder;
	}
	
	public void setPlayerOrder(Integer playerOrder) {
		this.playerOrder = playerOrder;
	}

	@Override
	public int compareTo(PlayerInfo pInfo) {
		if(this.getPlayerOrder() < pInfo.getPlayerOrder())
			return -1;
		else if(this.getPlayerOrder() > pInfo.getPlayerOrder())
			return 1;
		else 
			return 0;
	}
	
	
	
}
