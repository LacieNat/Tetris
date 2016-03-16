
import java.util.ArrayList;


public class PlayerSkeleton {
        
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves, ArrayList<Double> weights) {
            int move = 0;
            double eval = -1;
            for (int i = 0; i < legalMoves.length; i++) {
                TempState ts = new TempState(s.getField(), s.nextPiece, s.getTop(), s.getTurnNumber());
                ts.newMove(i);
                
                if(ts.hasLost())
                    continue;
                
                double curr;
                if((curr = Utility.computeStrategy(ts, weights))+ts.getRowsCleared()>eval) {
                    move = i;
                    eval = curr;
                }
            }

            return move;
	}
	
	public static void main(String[] args) {
		State s = new State();
		TFrame t = new TFrame(s);
                
		PlayerSkeleton p = new PlayerSkeleton();
                AI a = AI.newInstance(true);
 
                
                while(true) {
                    ArrayList<ArrayList<Double>> w = a.generateWeights();

                    for(int i=0; i<w.size(); i++) {
              
                        while(!s.hasLost()) {
                                System.out.println("Using w: " + w.get(i).toString());
                                s.makeMove(p.pickMove(s,s.legalMoves(), w.get(i)));
                                s.draw();
                                s.drawNext(0,0);
                                try {
                                        Thread.sleep(300);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                        }
                        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
                        a.addIteration(s.getRowsCleared());
                        s = new State();
                        t.bindState(s);
                    }                   
                }
	}
	
}
