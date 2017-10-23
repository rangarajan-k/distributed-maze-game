//package edu.nus.mazegame.trackerservice;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface TrackerService extends Remote {
	
	public String helloTo(String name) throws RemoteException;
	
	public String joinMazeGame(String newPlayerIp, String newPlayerPort, String newPlayerName) throws RemoteException;
	
	public HashMap<String, Object> getMazeGameInfo() throws RemoteException;
	
	public boolean removeCrashedPlayer(String playerName) throws RemoteException;

}
