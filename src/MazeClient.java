
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


/**
 *
 * @author ranga
 */
public class MazeClient extends javax.swing.JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// Variables declaration all the UI components and the supporting info variables.
	public int N;
    public int K;
    private String playerIp;
 	private String playerPort;
 	private String playerId;
 	private String mazeServerIp;
 	private String mazeServerPort;
 	private String backupServerIp;
 	private String backupServerPort;
    private GameService look_up;
    private BackupService look_up_backup_service;
	public SinglePlayerGameStates player;
	public SinglePlayerGameStates primaryServerData;
	public SinglePlayerGameStates backupServerData;
    private javax.swing.JLabel backupServerLabel;
    private javax.swing.JButton inputButton;
    private javax.swing.JTextField inputField;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel moveLabel;
    private javax.swing.JLabel primaryServerLabel;
    private javax.swing.JLabel scoreLabel;
    private javax.swing.JLabel welcomeLabel;
    private javax.swing.JTable gameTable;
    private static javax.swing.JFrame mainFrame;
    private InputStream inputStream;
    private boolean keepPollingInput = true;
    //private int mainFrameWidth = 1500;
    //private int mainFrameHeight = 1500;
 
    // End of variables declaration  
    
    
    public GameService getLook_up() {
		return look_up;
	}

	public boolean setLook_up(String look_up_str) {
		try {
			look_up = (GameService)Naming.lookup(look_up_str);			
			System.out.println("Successfully binded with new primary server");
			return true;
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public  BackupService getLook_up_backup_service() {
		return look_up_backup_service;
	}

	public  void setLook_up_backup_service(String look_up_backup_service_str) {
		try {
			look_up_backup_service = (BackupService) Naming.lookup(look_up_backup_service_str);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /*
	 * Constructor. Takes all the required parameters and binds the server RMI to a local variable.
	 */
    public MazeClient(String playerId, String mazeServerIp, String mazeServerPort, String playerIp, String playerPort, String backupServerIp, String backupServerPort, int n, String status) throws MalformedURLException, RemoteException, NotBoundException {
		this.mazeServerIp = mazeServerIp;
		this.mazeServerPort = mazeServerPort;
		this.playerIp = playerIp;
		this.playerPort = playerPort;
		this.backupServerIp = backupServerIp;
		this.backupServerPort = backupServerPort;
		this.playerId = playerId;
		this.N = n;
		
		look_up = (GameService) Naming.lookup("//"+mazeServerIp+"/GameService");
		
		/*if(status.equals(Game.MAZE_PLAYER)) {
			look_up_backup_service = (BackupService) Naming.lookup("//"+backupServerIp+"/GameBackupService");
		}*/
	}
	
	/*
	 * Called after constructor initializes variables, from the Game.java file. Tries to register the player with the server and then
	 * fires up the UI of the game client if join successful. Returns joinstatus
	 */
	public int joinMazeGame() {
		
		System.out.println("Player Id "+playerId);
		System.out.println("Player Ip "+playerIp);
		System.out.println("Player Port "+playerPort);	
		
		int joinStatus = 0;
	
		try {
			player = look_up.addPlayer(playerId,playerIp,playerPort);
	
			
			//if player joined successfuly initialise the client gui for this client
			if(player.getId()!= "") {
				joinStatus = 1;
				System.out.println("Successfully connected\n");
				initGUIComponents();
				drawPlayerOnGrid(player);
				drawGameMap(player);
				setServerLabels();
				setPlayerScore(player);
				//startPollingInputStream();
				
			} else {
				//TODO show dialog that cannot join with appropriate error message
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return joinStatus;
	}
                               

    /*
     * Supporting functions for the UI events goes here 
     */
	
     /*
      * The Menu exit function (menu on top left). Exits the game. 
     */
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
    	
    	int option = JOptionPane.showConfirmDialog(
                mainFrame, 
                "Are you sure you want to close the application?",
                "Close Confirmation", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
                System.exit(0);
        }
    }                                          
    
    /*
     *  The main "Send" input button event. When pressed sends the input to the server after validation. When response got, if valid
     *  updates game client with the new game state. Calls the makeGameMove API.
     */
    private void inputButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        /*Send move to server after validation*/
    	String input = inputField.getText();
    	List<String> validInputs = new ArrayList<String>(Arrays.asList("0","1","2","3","4","9")); 
    	if(validInputs.contains(input) )
    	{	
	    	boolean moveSuccess = makeGameMove(input);
	    	if(moveSuccess){
	    		JOptionPane.showMessageDialog(mainFrame, "Move success, refreshing gamestate");
	    		drawGameMap(player);
	    		setServerLabels();
				setPlayerScore(player);
	    		
	    	}
	    	else{
	    		JOptionPane.showMessageDialog(mainFrame, "Invalid move, please try again");
	    	}
    	}
    	else{
    		JOptionPane.showMessageDialog(mainFrame, "Please enter a valid move");
    	}
    	inputField.setText("");
    	
    	
    }    

    
    /*
     * The API, helper functions that will interact with the server , process info from server goes here
     */
	
    /*
	 * Called initially when the player registers with the server. using the coordinates got from the server, 
	 * places the player ID text on the game grid.
	 */
    public void updateUI(){
    	drawGameMap(player);
		setServerLabels();
		setPlayerScore(player);
    }
	public void drawPlayerOnGrid(SinglePlayerGameStates player){
		String id = player.id;
		int x = player.getXCoord();
		int y = player.getYCoord();
		
		TableModel model = gameTable.getModel();
		model.setValueAt(id, x, y);
		
	}
	
	public String[][] getGameMap() {
		String[][] gameMap = null;
		
		try {
			gameMap = look_up.getGameMap();
		} catch (RemoteException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return gameMap;
	}
	
	public Hashtable<String, SinglePlayerGameStates> getGameStatusInfo(){
		Hashtable<String, SinglePlayerGameStates> playerStats =
	            new Hashtable<String, SinglePlayerGameStates>();
		
		try {
			playerStats = (Hashtable<String, SinglePlayerGameStates>) look_up.getGameStatus();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return playerStats;
	}
	
	
	public BackupGameState getBackupGameState() {
		BackupGameState gameState = null;
		
		try {
			gameState = look_up_backup_service.getGameState();
			
		
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return gameState;
	}
	
	public void pingPrimary(String playerId) throws RemoteException,NotBoundException{
		
		//System.out.println("Marking Attendance");		
		this.getLook_up().updatePresense(playerId);
		
	}
	
	public int getPlayerOrder(String playerId) throws RemoteException{
		return this.getLook_up().getPlayerOrder(playerId);
	}
	
    /*
     * Called when making a game move. use RMI look_up to contact server function movePlayer. Called when Send button pressed
     */
	public boolean makeGameMove(String input){
		try {
			System.out.println(player.getId()+" is making the move "+input);
			int move_success =look_up.movePlayer(player.getId(), input);
			if(move_success != -1){
				String[][] gameMap = look_up.getGameMap();
				Hashtable<String, SinglePlayerGameStates> playerStats = look_up.getGameStatus();
				
				//Update the Backupserver of new Move
				//updateBackupServer(gameMap, playerStats);
				
				return true;
			}
			else if(move_success == -1){
				return false;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch blockj
			e.printStackTrace();
		}
		return true;
	}
	
	/*public void updateBackupServer(String[][] gameMap, Hashtable<String, SinglePlayerGameStates> playerStats) {
		try {
			System.out.println("Updating Backup");
			
			
			look_up_backup_service.updateGameState(playerStats, gameMap);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}*/
	/*
	 * Critical functioin that contacts the RMI of server to get the latest game state and draws the gamemap (game state) on
	 * the client side with the updated info.
	 */
	public void drawGameMap(SinglePlayerGameStates player){
		int i,j;
		try {
			String [][] gameMap = look_up.getGameMap();			
			TableModel model = gameTable.getModel();
	
			for(i=0;i<N;i++){
				for(j=0;j<N;j++){					
					model.setValueAt(" ", i, j);					
				}
			}
			for(i=0;i<N;i++){
				for(j=0;j<N;j++){
					if (gameMap[i][j] == null){
						gameMap[i][j] = " ";
					}
					//System.out.print(gameMap[i][j]+ " \t");
					model.setValueAt(gameMap[i][j], i, j);
				}
				//System.out.print("\n");
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/*
	 * Helper function that sets the server address of primary and backup server in the UI.
	 */
	public void setServerLabels(){
		try {
			LinkedList<String> playerOrderList = look_up.getPlayerOrderList();
			String pServer = playerOrderList.get(0);
			String bServer = null;
			if(playerOrderList.size() > 1){
				bServer = playerOrderList.get(1);
				//backupServerData = look_up.getPlayerState(bServer);
			}
			//primaryServerData = look_up.getPlayerState(pServer);
			
			
			primaryServerLabel.setText("Primary Server is " + pServer.toUpperCase());
			if(bServer != null && !bServer.isEmpty()){
				backupServerLabel.setText("Backup Server is " + bServer.toUpperCase());
			}else{
				backupServerLabel.setText(" ");
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/*
	 * Helper function that sets the player score text on the UI.
	 */
	public void setPlayerScore(SinglePlayerGameStates localPlayer){
		try {
			localPlayer = look_up.getPlayerState(player.getId());
			
			scoreLabel.setText("Your score is "+localPlayer.getScore());
			player.setScore(localPlayer.getScore());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void startPollingInputStream(){
		keepPollingInput = true;
		
		Thread inputReader = new Thread(){
			public void run(){
				
				while (keepPollingInput) {
					
					System.out.println("Enter the move: ");
					Scanner scanner = new Scanner(System.in);
	                if(scanner.hasNext()){
	                String inputMove = scanner.nextLine();
		                if(Integer.parseInt(inputMove) > 4 || Integer.parseInt(inputMove) < 0){
		                	System.out.println("Enter valid move");
		                }
		                else{
		                	String input = inputMove;
		                	boolean moveSuccess = makeGameMove(input);
		                	/*try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}*/
		                	if(moveSuccess){
		                		System.out.println("Move made successfully, game state refreshed");
		                		drawGameMap(player);
		                		setServerLabels();
		        				setPlayerScore(player);
		                	}
		                	else{
		                		System.out.println("Error in making move");
		                	}
		                }
	                }
	                //scanner.close();
	                
		        } 
			}
		};
		inputReader.start();
		
	}
	
	public void stopPollingInputStream(){
		keepPollingInput = false;
	}
	 /*Main function to call to generate and fire up the GUI, should be called after the client has been registered
     * with the primary server. No arguments and no return values.
     */
    public void initGUIComponents() {

        jPanel1 = new javax.swing.JPanel();        
        jPanel2 = new javax.swing.JPanel();
        welcomeLabel = new javax.swing.JLabel();
        inputField = new javax.swing.JTextField();
        moveLabel = new javax.swing.JLabel();
        inputButton = new javax.swing.JButton();
        primaryServerLabel = new javax.swing.JLabel();
        backupServerLabel = new javax.swing.JLabel();
        scoreLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jScrollPane1 = new javax.swing.JScrollPane();
        mainFrame = new javax.swing.JFrame();
        
        jScrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        gameTable = new JTable(); 
        gameTable.setModel(new MyTableModel(N));
        
        
        gameTable.setPreferredScrollableViewportSize(new Dimension(500, 500));
        //table.setFillsViewportHeight(true);
        gameTable.setGridColor(new Color(0,0,0));
        gameTable.setShowGrid(true);
        gameTable.setTableHeader(null); 
        jScrollPane1.setViewportView(gameTable);
        

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );

        welcomeLabel.setText("Welcome to Maze game " + player.getId());

        moveLabel.setText("Please given an input and press send");

        inputButton.setText("Send");
        inputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	inputButtonActionPerformed(evt);
            }
        });

        primaryServerLabel.setText("Primary server IP");

        backupServerLabel.setText("Backup server IP");

        scoreLabel.setText("Score");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(primaryServerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(welcomeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moveLabel)
                    .addComponent(backupServerLabel)
                    .addComponent(scoreLabel))
                .addContainerGap(140, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(welcomeLabel)
                .addGap(51, 51, 51)
                .addComponent(moveLabel)
                .addGap(18, 18, 18)
                .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(inputButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(59, 59, 59)
                .addComponent(primaryServerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(backupServerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(scoreLabel)
                .addContainerGap(304, Short.MAX_VALUE))
        );

        jMenu1.setText("Menu");

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        mainFrame.setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(mainFrame.getContentPane());
        mainFrame.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        mainFrame.pack();
        /*
         * Setting the UI theme, some error need to look if time there. Not at all important
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MazeClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MazeClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MazeClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MazeClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        */

        /* Create and display the form */
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /*mainFrame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {                
                e.getWindow().dispose();
                System.exit(0);
            }
        });
        */
        mainFrame.setVisible(true);
        
    }
    
    /*
     * Custom table model class supporting the gamegrid generation on the UI. Initializes the empty table of size N x N. 
     * Contains supporting functions to get and set values on the grid table.
     */
	class MyTableModel extends AbstractTableModel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String[] columnNames = new String[N];
        public Object[][] data = new Object[N][N];  
        
        public MyTableModel(int n){
        	int i,j;
        	for (i=0;i<n;i++){
        		columnNames[i] = " ";
        	} 
        	for (i=0;i<n;i++){
	            for(j=0;j<n;j++){
	            	data[i][j] = " ";
	            }
            }
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        //get value at any position
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        //set value of a particular cell in the table
        public void setValueAt(Object value, int row, int col) {
            
            data[row][col] = value;
            fireTableCellUpdated(row, col);
           
        }
        /*
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        */
    
    }

}
