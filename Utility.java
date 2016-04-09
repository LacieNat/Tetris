
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
    public static int numOfWeights = 6;
    public static double computeStrategy(TempState s, State st, int[] move) { 
//        printField(s.getField());
//        System.out.println(move[State.SLOT]);
        double featureSum = -4.500158825082766*findLandingHeight(st.getField(), move[State.SLOT], 
                columnHeights(st.getField()),State.pHeight[st.nextPiece][move[State.ORIENT]],
                State.pWidth[st.nextPiece][move[State.ORIENT]])
                + 3.4181268101392694* (s.getRowsCleared()-st.getRowsCleared())
                -3.2178882868487753*rowTransitions(s.getField())
                -9.348695305445199*columnTransitions(s.getField())
                -7.899265427351652*numOfHoles(s.getField(), columnHeights(s.getField()))
                -3.3855972247263626*wells(s.getField());
        
        return featureSum;
    }
    
    public static double computeStrategyLearning(TempState t, State s, int[] move, double[] w) {
        return  w[0]*findLandingHeight(s.getField(), move[State.SLOT], 
                columnHeights(s.getField()),State.pHeight[s.nextPiece][move[State.ORIENT]],
                State.pWidth[s.nextPiece][move[State.ORIENT]])
                + w[1]* (t.getRowsCleared()-s.getRowsCleared())
                + w[2]*rowTransitions(t.getField())
                + w[3]*columnTransitions(t.getField())
                + w[4]*numOfHoles(t.getField(), Utility.columnHeights(t.getField()))
                + w[5]*wells(t.getField());
    }
    
    public static void printField(int[][] field) {
        for(int i=field.length-1; i>=0; i--) {
            System.out.println();
            for(int j= 0; j<field[i].length; j++) {
                System.out.print(field[i][j]);
            }
        }
        System.out.println();
        System.out.println();
    }
    
    public static int numOfHoles(int[][] field, int[] colHeight) {
        int holes = 0;
        
        //to count the number of holes, count the empty blocks from bottom to the top of the column
        for(int i=0; i<State.COLS; i++) {
            for(int j=colHeight[i]-2; j>=0; j--) {
                if(field[j][i]==0) {
                    holes++;
                }
            }
        }
        return holes;
    }
    
    public static int wells(int[][] field) {
        int wellSum = 0;
        int test = 0;
        
        //Case inner columns
        for(int i=1; i<State.COLS-1; i++) {
            int inc = 1;
            int sum = 0;
            test++;
            for(int j=field.length-1; j>=0; j--) {
                if(field[j][i]==0 && field[j][i-1]!=0 && field[j][i+1]!=0) {
                    sum+=inc;
                    inc++;
                    test++;
                    for(int k=j-1; k>=0; k--) {
                        if(field[k][i]==0) {
                            sum+=inc;
                            inc++;
                            test++;
                        } else{
                            wellSum+=sum;
                            break;
                        }
                    }
                }
            }
        }
        
        //Case if well is on the left side of the wall
        for(int j=field.length-1; j>=0; j--) {
            int inc = 1;
            int sum = 0;
            if(field[j][0]==0 && field[j][1]!=0) {
                sum+=inc;
                inc++;
                test++;
                for(int k=j-1; k>=0; k--) {
                    if(field[k][0]==0) {
                        sum+=inc;
                        inc++;
                        test++;
                    } else{
                        wellSum+=sum;
                        break;
                    }
                }
            }
        }
        
        //Case if well is on the right side of the wall
        for(int j=field.length-1; j>=0; j--) {
            int inc = 1;
            int sum = 0;
            if(field[j][State.COLS-1]==0 && field[j][State.COLS-2]!=0) {
                sum+=inc;
                inc++;
                test++;
                for(int k=j-1; k>=0; k--) {
                    if(field[k][State.COLS-1]==0) {
                        sum+=inc;
                        inc++;
                        test++;
                    } else{
                        wellSum+=sum;
                        break;
                    }
                }
            }
        }
        return test;
    }
    
    public static int findLandingHeight(int[][] field, int slot, int[] colHeight, int pHeight, int pWidth) {
        int max = colHeight[slot];
        for(int i=slot+1; i<pWidth; i++) {
            if(colHeight[i]>max) {
                max = colHeight[i];
            }
        }
//        System.out.println(max);
//        printField(field);
        
        return max + (pHeight-1)/2;
    }
    
    public static int rowTransitions(int[][] field) {
        int tr = 0;
        
        for(int i=0; i<field.length; i++) {
            boolean isEmptyCell=field[i][0]==0;
            for(int j=1; j<field[i].length; j++) {
                if(isEmptyCell && field[i][j]!=0) {
                    tr++;
                }
                
                else if(!isEmptyCell && field[i][j]==0) {
                    tr++;
                }
                
                isEmptyCell = field[i][j]==0;
            }
        }
        return tr;
    }
    
    public static int columnTransitions(int[][] field) {
        int tr = 0;
        boolean isEmptyCell;
        
        for(int i=0; i<State.COLS; i++){
            isEmptyCell = field[0][i]==0;
            for(int j=1; j<State.ROWS; j++) {
                if(isEmptyCell && field[j][i]!=0) {
                    tr++;
                }
                
                if(!isEmptyCell && field[j][i]==0) {
                    tr++;
                }
                
                isEmptyCell = field[j][i]==0;
            }
        }
            
        return tr;
        
    }
    
    public static int[] columnHeights(int[][] field) {
        int[] col = new int[10];
        
        for(int i=0; i<State.COLS; i++) {
            for(int j=field.length-1; j>=0; j--) {
                if(field[j][i]!=0) {
                    col[i]=j+1;
                    break;
                }
            }
        }
        
        
        return col;
    }
}
