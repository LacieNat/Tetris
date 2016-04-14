
public class PlayerSkeleton {
	private int linesCleared = 0;
	
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int move = 0;
        double eval = Integer.MIN_VALUE;
        for (int i = 0; i < legalMoves.length; i++) {
            TempState ts = new TempState(s.getField(), s.nextPiece, s.getTop(), s.getTurnNumber(), s.getRowsCleared());
            ts.newMove(i);
            
            if(ts.hasLost())
                continue;
            
            double curr = Utility.computeStrategy(ts, s, legalMoves[i]);
            if(curr>eval) {
                move = i;
                eval = curr;
            }
        }
        
        linesCleared = s.getRowsCleared();
        System.out.println("Lines cleared: " + linesCleared);

        return move;
	}
	
	
	public static void main(String[] args) {
		State s = new State();
		new TFrame(s);
		PlayerSkeleton p = new PlayerSkeleton();
		while(!s.hasLost()) {
			s.makeMove(p.pickMove(s,s.legalMoves()));
			s.draw();
			s.drawNext(0,0);
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
	}

}
