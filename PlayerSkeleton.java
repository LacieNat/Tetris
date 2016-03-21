
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import net.sourceforge.jswarm_pso.Particle;
import net.sourceforge.jswarm_pso.Swarm;


public class PlayerSkeleton {
        
	//implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves, double[] w) {
            int move = 0;
            double eval = Integer.MIN_VALUE;
            for (int i = 0; i < legalMoves.length; i++) {
                TempState ts = new TempState(s.getField(), s.nextPiece, s.getTop(), s.getTurnNumber(), s.getRowsCleared());
                ts.newMove(i);
                
                if(ts.hasLost())
                    continue;
                
                double curr = Utility.computeStrategyLearning(ts, s, legalMoves[i], w);
//                System.out.println("curr " + curr);
                if(curr>eval) {
//                    System.out.println("chaange move");
                    move = i;
                    eval = curr;
                }
            }

            return move;
	}
	
	public static void main(String[] args) {
		State s = new State();
		//TFrame t = new TFrame(s);
                
		PlayerSkeleton p = new PlayerSkeleton();
                TetrisFitnessFunction tff = new TetrisFitnessFunction();
                Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES, new Weight(), tff);
                swarm.setMaxPosition(10);
                swarm.setMinPosition(-10);
                swarm.init();
                swarm.setInertia(0.95);  
                swarm.setGlobalIncrement(0.9);  
                swarm.setParticleIncrement(0.9);
               
                
                int iter = 10000;
                for(int i=0; i<iter; i++) {
                   Particle[] particles = swarm.getParticles();
                   tff.setParticles(particles);
                   tff.clearHashMap();
                  
                   for(int j=0; j<particles.length; j++) {
                       Particle currParticle = particles[j];
                       double[] currWeight = particles[j].getPosition();
                        while(!s.hasLost()) {

                                s.makeMove(p.pickMove(s,s.legalMoves(), currWeight));
//                                s.draw();
//                                s.drawNext(0,0);
//                                try {
//                                        Thread.sleep(300);
//                                } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                }
//                            System.out.println("You have completed "+s.getRowsCleared()+" rows.");

                        }
                        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
                        tff.set(j, s.getRowsCleared());
                        s = new State();
//                        t.bindState(s);
                   }
                   
                   swarm.evolve();
                }
                System.out.println(swarm.toStringStats());            

	}
	
}
