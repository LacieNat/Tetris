import java.util.concurrent.*;
import java.util.*;

public class PlayerSkeleton {

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves, double[] w) {
		int move = 0;
		double eval = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++) {
			TempState ts = new TempState(s.getField(), s.nextPiece, s.getTop(),
					s.getTurnNumber(), s.getRowsCleared());
			ts.newMove(i);

			if (ts.hasLost())
				continue;

			double curr = Utility.computeStrategyLearning(ts, s, legalMoves[i],
					w);
			if (curr > eval) {
				move = i;
				eval = curr;
			}
		}

		return move;
	}

	public static class StateCallable implements Callable<Void> {
		private final Particle p;
		private final double[] w;
		private final PlayerSkeleton ps;
		private final TetrisFitnessFunction tff;
		private final int index;

		public StateCallable(PlayerSkeleton ps, Particle p, double[] w,
				TetrisFitnessFunction tff, int index) {
			this.p = p;
			this.w = w;
			this.ps = ps;
			this.tff = tff;
			this.index = index;
		}

		public Void call() throws Exception {
			State s = new State();
			while (!s.hasLost()) {

				s.makeMove(ps.pickMove(s, s.legalMoves(), w));
				// s.draw();
				// s.drawNext(0,0);
				// try {
				// Thread.sleep(300);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// System.out.println("You have completed "+s.getRowsCleared()+" rows.");

			}
			System.out.println("You have completed " + s.getRowsCleared()
					+ " rows.");
			tff.set(index, s.getRowsCleared());
			return null;
		}
	}

	public static void main(String[] args) throws Exception {

		State s = new State();
		// TFrame t = new TFrame(s);

		PlayerSkeleton p = new PlayerSkeleton();
		TetrisFitnessFunction tff = new TetrisFitnessFunction();
		Swarm swarm = new Swarm(Swarm.DEFAULT_NUMBER_OF_PARTICLES,
				new Weight(), tff);
		swarm.setMaxPosition(10);
		swarm.setMinPosition(-10);
		swarm.init();
		swarm.setInertia(0.95);
		swarm.setGlobalIncrement(0.9);
		swarm.setParticleIncrement(0.9);

		int iterCon = SaveParticles.loadState(swarm.getParticles());

		int iter = 10;

		// Parallel Setup
		ExecutorService es = Executors
				.newFixedThreadPool(swarm.getParticles().length);
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();

		for (int i = iterCon; i < iterCon + iter; i++) {
			Particle[] particles = swarm.getParticles();
			tff.setParticles(particles);
			tff.clearHashMap();
			callables.clear();

			for (int j = 0; j < particles.length; j++) {
				Particle currParticle = particles[j];
				double[] currWeight = particles[j].getPosition();

				callables.add(new StateCallable(p, currParticle, currWeight,
						tff, j));

				// s = new State();
				// t.bindState(s);
			}
			es.invokeAll(callables);
			swarm.evolve();
			System.out.println("ITERATION " + i);
			System.out.println(swarm.toStringStats());
			SaveParticles.saveSate(i, particles);
		}
		System.out.println(swarm.toStringStats());
		es.shutdown();

	}

}
