
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lacie
 */
public class Utility {
    
    public static double computeStrategy(TempState s, ArrayList<Double> weights) {
        double featureSum = weights.get(0);
        int[] colHeights = columnHeights(s.getField());
        int[] adjCol = adjColDiff(colHeights);
        
        for(int i=0; i<colHeights.length; i++) {
            featureSum += weights.get(i+1)*colHeights[i];
        }
        
        for(int i=0; i<adjCol.length; i++) {
            featureSum += weights.get(i+11)*adjCol[i];
        }
        
        featureSum += weights.get(20)*maxHeight(colHeights) + weights.get(21)*numOfHoles(s.getField(), colHeights);
        
        return featureSum;
    }
    
    public static int numOfHoles(int[][] field, int[] colHeight) {
        int holes = 0;
        
        //to count the number of holes, count the empty blocks from bottom to the top of the column
        for(int i=0; i<colHeight.length; i++) {
            for(int j=0; j<colHeight[i]; j++) {
                if(field[j][i]==0) {
                    holes++;
                }
            }
        }
//        for(int i=0; i<maxHeight(colHeight); i++) {
//            for(int j=0; j<field[i].length; j++) {
//                //case 1: hole is at the left side of the wall
//                if(field[i][j]==0) {
//                    if(j==0) {
//                        if(i== 19) {
//                            if(field[i-1][j]!=0 || field[i][j+1]!=0) {
//                                holes++;
//                            }
//                        }
//                        else if(i!=0 && (field[i][j+1]!=0 || field[i-1][j]!=0 || field[i+1][j]!=0)){            
//                            holes++;
//                        }
//                    }
//
//                    //case 2: hole is at the right side of the wall
//                    else if(j==field[i].length-1){
//                        if(i==19) {
//                            if(field[i][j-1]!=0 || field[i-1][j]!=0) {
//                                holes++;
//                            }
//                        }
//                        else if(i!=0 && (field[i][j-1]!=0 &&field[i-1][j]!=0 || field[i+1][j]!=0)) {
//                            holes++;
//                        }
//                    }
//
//                    //case 3: hole is in the middle block by right and left
//                    else if(i!=0 && i!=19 && (field[i][j-1]!=0 || field[i][j+1]!=0 || field[i+1][j]!=0)) {
//                        holes++;
//                    }
//
//                    else if(i==19 && (field[i][j-1]!=0 || field[i][j+1]!=0 || field[i-1][j]!=0)) {
//                        holes++;
//                    }
//                }
           // }
        //}
        
        return holes;
    }
    
    public static int findLandingHeight(int[][] field, int slot) {
        for(int i=0; i<field.length; i++) {
            if(field[i][slot]!=0) {
                return 20-i;
            }
        }
        return 0;
    }
    
    public static int erodedCells(int rowsCleared) {
        return rowsCleared * 10;
    }
    
    public static int[] columnHeights(int[][] field) {
        int[] col = new int[10];
        
        for(int i=0; i<field.length; i++) {
            for(int j=0; j<field[i].length; j++) {
                if(field[i][j]!= 0) {
                    col[j] = 20-i;
                }
            }
        }
        
        return col;
    }
    
    public static int maxHeight(int[] col) {
        int max = -1;
        
        for(int i=0; i<col.length; i++) {
            if (col[i]>max) {
                max = col[i];
            }
        }
        
        return max;
    }
    
    public static int[] adjColDiff(int[] col) {
        int diff[] = new int[9];
        
        for(int i=0; i<diff.length; i++) {
            diff[i] = Math.abs(col[i+1]-col[i]);
        }
        
        return diff;
    }
}
