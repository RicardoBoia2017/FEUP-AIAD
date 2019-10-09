import java.util.Arrays;

public class Map {
	private int[][] map;
	private int length, heigth;
	
	Map(int length, int heigth){
		map = new int[heigth][length];
		
		for (int[] row: map)
		    Arrays.fill(row, 0);
	
	    }

	public void print() {
		
	    for(int i=0 ; i < heigth ; i++){
	        for(int j = 0; j < length ; j ++){
	            System.out.printf("\t %d \t",map[i][j]);
	        }
	        System.out.println();
	    }
 }
}