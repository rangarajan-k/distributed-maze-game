import java.io.Serializable;

public class GameMap implements Serializable {
	String[][] returnMap;
	GameMap(int n,String[][] mapArray){
		for (int i=0;i<n;i++){
			for (int j=0;j<n;j++){
				this.returnMap[i][j] = mapArray[i][j];
			}
		}
	}
}
