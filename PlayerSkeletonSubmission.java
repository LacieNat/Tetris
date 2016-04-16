import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class PlayerSkeletonSubmission {
	private int linesCleared = 0;

	// implement this function to have a working system
	public int pickMove(State s, int[][] legalMoves) {
		int move = 0;
		double eval = Integer.MIN_VALUE;
		for (int i = 0; i < legalMoves.length; i++) {
			TempState ts = new TempState(s.getField(), s.nextPiece, s.getTop(), s.getTurnNumber(), s.getRowsCleared());
			ts.newMove(i);

			if (ts.hasLost())
				continue;

			double curr = Utility.computeStrategy(ts, s, legalMoves[i]);
			if (curr > eval) {
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
		PlayerSkeletonSubmission p = new PlayerSkeletonSubmission();
		while (!s.hasLost()) {
			s.makeMove(p.pickMove(s, s.legalMoves()));
			s.draw();
			s.drawNext(0, 0);
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("You have completed " + s.getRowsCleared() + " rows.");
	}

	/**
	 *
	 * @author lacie
	 */
	public static class Utility {
		public static int numOfWeights = 6;

		public static double computeStrategy(TempState s, State st, int[] move) {
			// printField(s.getField());
			// System.out.println(move[State.SLOT]);

			double featureSum = -4.500158825082766
					* findLandingHeight(st.getField(), move[State.SLOT], columnHeights(st.getField()),
							State.pHeight[st.nextPiece][move[State.ORIENT]],
							State.pWidth[st.nextPiece][move[State.ORIENT]])
					+ 3.4181268101392694 * (s.getRowsCleared() - st.getRowsCleared())
					- 3.2178882868487753 * rowTransitions(s.getField())
					- 9.348695305445199 * columnTransitions(s.getField())
					- 7.899265427351652 * numOfHoles(s.getField(), columnHeights(s.getField()))
					- 3.3855972247263626 * wells(s.getField());

			return featureSum;
		}

		public static double computeStrategyLearning(TempState t, State s, int[] move, double[] w) {
			return w[0] * findLandingHeight(s.getField(), move[State.SLOT], columnHeights(s.getField()),
					State.pHeight[s.nextPiece][move[State.ORIENT]], State.pWidth[s.nextPiece][move[State.ORIENT]])
					+ w[1] * (t.getRowsCleared() - s.getRowsCleared()) + w[2] * rowTransitions(t.getField())
					+ w[3] * columnTransitions(t.getField())
					+ w[4] * numOfHoles(t.getField(), Utility.columnHeights(t.getField())) + w[5] * wells(t.getField());
		}

		public static void printField(int[][] field) {
			for (int i = field.length - 1; i >= 0; i--) {
				System.out.println();
				for (int j = 0; j < field[i].length; j++) {
					System.out.print(field[i][j]);
				}
			}
			System.out.println();
			System.out.println();
		}

		public static int numOfHoles(int[][] field, int[] colHeight) {
			int holes = 0;

			// to count the number of holes, count the empty blocks from bottom
			// to
			// the top of the column
			for (int i = 0; i < State.COLS; i++) {
				for (int j = colHeight[i] - 2; j >= 0; j--) {
					if (field[j][i] == 0) {
						holes++;
					}
				}
			}
			return holes;
		}

		public static int wells(int[][] field) {
			int wellSum = 0;
			int test = 0;

			// Case inner columns
			for (int i = 1; i < State.COLS - 1; i++) {
				int inc = 1;
				int sum = 0;
				test++;
				for (int j = field.length - 1; j >= 0; j--) {
					if (field[j][i] == 0 && field[j][i - 1] != 0 && field[j][i + 1] != 0) {
						sum += inc;
						inc++;
						test++;
						for (int k = j - 1; k >= 0; k--) {
							if (field[k][i] == 0) {
								sum += inc;
								inc++;
								test++;
							} else {
								wellSum += sum;
								break;
							}
						}
					}
				}
			}

			// Case if well is on the left side of the wall
			for (int j = field.length - 1; j >= 0; j--) {
				int inc = 1;
				int sum = 0;
				if (field[j][0] == 0 && field[j][1] != 0) {
					sum += inc;
					inc++;
					test++;
					for (int k = j - 1; k >= 0; k--) {
						if (field[k][0] == 0) {
							sum += inc;
							inc++;
							test++;
						} else {
							wellSum += sum;
							break;
						}
					}
				}
			}

			// Case if well is on the right side of the wall
			for (int j = field.length - 1; j >= 0; j--) {
				int inc = 1;
				int sum = 0;
				if (field[j][State.COLS - 1] == 0 && field[j][State.COLS - 2] != 0) {
					sum += inc;
					inc++;
					test++;
					for (int k = j - 1; k >= 0; k--) {
						if (field[k][State.COLS - 1] == 0) {
							sum += inc;
							inc++;
							test++;
						} else {
							wellSum += sum;
							break;
						}
					}
				}
			}
			return test;
		}

		public static double findLandingHeight(int[][] field, int slot, int[] colHeight, int pHeight, int pWidth) {
			int max = colHeight[slot];
			for (int i = slot + 1; i < pWidth; i++) {
				if (colHeight[i] > max) {
					max = colHeight[i];
				}
			}
			// System.out.println(max);
			// printField(field);
			return (max + (pHeight / 2));
		}

		public static int rowTransitions(int[][] field) {
			int tr = 0;

			for (int i = 0; i < field.length; i++) {
				boolean isEmptyCell = field[i][0] == 0;
				for (int j = 1; j < field[i].length; j++) {
					if (isEmptyCell && field[i][j] != 0) {
						tr++;
					}

					else if (!isEmptyCell && field[i][j] == 0) {
						tr++;
					}

					isEmptyCell = field[i][j] == 0;
				}
			}
			return tr;
		}

		public static int columnTransitions(int[][] field) {
			int tr = 0;
			boolean isEmptyCell;

			for (int i = 0; i < State.COLS; i++) {
				isEmptyCell = field[0][i] == 0;
				for (int j = 1; j < State.ROWS; j++) {
					if (isEmptyCell && field[j][i] != 0) {
						tr++;
					}

					if (!isEmptyCell && field[j][i] == 0) {
						tr++;
					}

					isEmptyCell = field[j][i] == 0;
				}
			}

			return tr;

		}

		public static int[] columnHeights(int[][] field) {
			int[] col = new int[10];
			for (int i = 0; i < State.COLS; i++) {
				for (int j = field.length - 1; j >= 0; j--) {
					if (field[j][i] != 0) {
						col[i] = j + 1;
						break;
					}
				}
			}
			return col;
		}
	}

	public static class TempState {
		public static final int COLS = 10;
		public static final int ROWS = 21;
		public static final int N_PIECES = 7;

		public boolean lost = false;
		// current turn
		private int turn = 0;
		private int cleared = 0;

		// each square in the grid - int means empty - other values mean the
		// turn it was placed
		private int[][] field = new int[ROWS][COLS];
		// top row+1 of each column
		// 0 means empty
		private int[] top = new int[COLS];

		// number of next piece
		protected int nextPiece;

		// all legal moves - first index is piece type - then a list of 2-length
		// arrays
		protected static int[][][] legalMoves = new int[N_PIECES][][];

		// indices for legalMoves
		public static final int ORIENT = 0;
		public static final int SLOT = 1;

		// possible orientations for a given piece type
		protected static int[] pOrients = { 1, 2, 4, 4, 4, 2, 2 };

		// the next several arrays define the piece vocabulary in detail
		// width of the pieces [piece ID][orientation]
		protected static int[][] pWidth = { { 2 }, { 1, 4 }, { 2, 3, 2, 3 }, { 2, 3, 2, 3 }, { 2, 3, 2, 3 }, { 3, 2 },
				{ 3, 2 } };
		// height of the pieces [piece ID][orientation]
		private static int[][] pHeight = { { 2 }, { 4, 1 }, { 3, 2, 3, 2 }, { 3, 2, 3, 2 }, { 3, 2, 3, 2 }, { 2, 3 },
				{ 2, 3 } };
		private static int[][][] pBottom = { { { 0, 0 } }, { { 0 }, { 0, 0, 0, 0 } },
				{ { 0, 0 }, { 0, 1, 1 }, { 2, 0 }, { 0, 0, 0 } }, { { 0, 0 }, { 0, 0, 0 }, { 0, 2 }, { 1, 1, 0 } },
				{ { 0, 1 }, { 1, 0, 1 }, { 1, 0 }, { 0, 0, 0 } }, { { 0, 0, 1 }, { 1, 0 } },
				{ { 1, 0, 0 }, { 0, 1 } } };
		private static int[][][] pTop = { { { 2, 2 } }, { { 4 }, { 1, 1, 1, 1 } },
				{ { 3, 1 }, { 2, 2, 2 }, { 3, 3 }, { 1, 1, 2 } }, { { 1, 3 }, { 2, 1, 1 }, { 3, 3 }, { 2, 2, 2 } },
				{ { 3, 2 }, { 2, 2, 2 }, { 2, 3 }, { 1, 2, 1 } }, { { 1, 2, 2 }, { 3, 2 } },
				{ { 2, 2, 1 }, { 2, 3 } } };

		// initialize legalMoves
		{
			// for each piece type
			for (int i = 0; i < N_PIECES; i++) {
				// figure number of legal moves
				int n = 0;
				for (int j = 0; j < pOrients[i]; j++) {
					// number of locations in this orientation
					n += COLS + 1 - pWidth[i][j];
				}
				// allocate space
				legalMoves[i] = new int[n][2];
				// for each orientation
				n = 0;
				for (int j = 0; j < pOrients[i]; j++) {
					// for each slot
					for (int k = 0; k < COLS + 1 - pWidth[i][j]; k++) {
						legalMoves[i][n][ORIENT] = j;
						legalMoves[i][n][SLOT] = k;
						n++;
					}
				}
			}

		}

		public int[][] getField() {
			return field;
		}

		public int[] getTop() {
			return top;
		}

		public static int[] getpOrients() {
			return pOrients;
		}

		public static int[][] getpWidth() {
			return pWidth;
		}

		public static int[][] getpHeight() {
			return pHeight;
		}

		public static int[][][] getpBottom() {
			return pBottom;
		}

		public static int[][][] getpTop() {
			return pTop;
		}

		public int getNextPiece() {
			return nextPiece;
		}

		public boolean hasLost() {
			return lost;
		}

		public int getRowsCleared() {
			return cleared;
		}

		public int getTurnNumber() {
			return turn;
		}

		// constructor
		public TempState() {
			nextPiece = randomPiece();

		}

		// random integer, returns 0-6
		private int randomPiece() {
			return (int) (Math.random() * N_PIECES);
		}

		// gives legal moves for
		public int[][] legalMoves() {
			return legalMoves[nextPiece];
		}

		// make a move based on the move index - its order in the legalMoves
		// list
		public void newMove(int move) {
			newMove(legalMoves[nextPiece][move]);
		}

		// make a move based on an array of orient and slot
		public void newMove(int[] move) {
			newMove(move[ORIENT], move[SLOT]);
		}

		// returns false if you lose - true otherwise
		public boolean newMove(int orient, int slot) {

			// System.out.println("ORIENT:" + orient + "SLOT:" + slot);

			turn++;
			// height if the first column makes contact

			int height = top[slot] - pBottom[nextPiece][orient][0];
			// for each column beyond the first in the piece
			for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
				height = Math.max(height, top[slot + c] - pBottom[nextPiece][orient][c]);
			}

			// check if game ended
			if (height + pHeight[nextPiece][orient] >= ROWS) {
				lost = true;
				return false;
			}

			// for each column in the piece - fill in the appropriate blocks
			for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

				// from bottom to top of brick
				for (int h = height + pBottom[nextPiece][orient][i]; h < height + pTop[nextPiece][orient][i]; h++) {
					field[h][i + slot] = turn;
				}
			}

			// adjust top
			for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
				top[slot + c] = height + pTop[nextPiece][orient][c];
			}

			int rowsCleared = 0;

			// check for full rows - starting at the top
			for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
				// check all columns in the row
				boolean full = true;
				for (int c = 0; c < COLS; c++) {
					if (field[r][c] == 0) {
						full = false;
						break;
					}
				}
				// if the row was full - remove it and slide above stuff down
				if (full) {
					rowsCleared++;
					cleared++;
					// for each column
					for (int c = 0; c < COLS; c++) {

						// slide down all bricks
						for (int i = r; i < top[c]; i++) {
							field[i][c] = field[i + 1][c];
						}
						// lower the top
						top[c]--;
						while (top[c] >= 1 && field[top[c] - 1][c] == 0)
							top[c]--;
					}
				}
			}

			// pick a new piece
			nextPiece = randomPiece();

			return true;
		}

		// extra functions

		public TempState(int[][] newField, int newNextPiece, int[] newTop, int newTurn, int cleared) {

			// http://stackoverflow.com/questions/1564832/how-do-i-do-a-deep-copy-of-a-2d-array-in-java
			// only way to do a deep copy is to iterate through the array and
			// copy each element
			for (int i = 0; i < newField.length; i++) {
				for (int j = 0; j < newField[i].length; j++) {
					this.field[i][j] = newField[i][j];
				}
			}

			this.nextPiece = newNextPiece;

			this.turn = newTurn;

			this.cleared = cleared;

			for (int i = 0; i < newTop.length; i++) {
				this.top[i] = newTop[i];
			}

		}

		public int[][][] allLegalMoves() {
			return legalMoves;
		}
	}
	
}


//learning code

/**
 * Base Fitness Function
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public abstract class FitnessFunction {

	*//** Should this funtion be maximized or minimized *//*
	boolean maximize;

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------

	*//** Default constructor *//*
	public FitnessFunction() {
		maximize = true; // Default: Maximize
	}

	*//**
	 * Constructor 
	 * @param maximize : Should we try to maximize or minimize this function?
	 *//*
	public FitnessFunction(boolean maximize) {
		this.maximize = maximize;
	}

	//-------------------------------------------------------------------------
	// Methods
	//-------------------------------------------------------------------------

	*//**
	 * Evaluates a particles at a given position
	 * NOTE: You should write your own method!
	 * 
	 * @param position : Particle's position
	 * @return Fitness function for a particle
	 *//*
	public abstract double evaluate(double position[]);

	*//**
	 * Evaluates a particles 
	 * @param particle : Particle to evaluate
	 * @return Fitness function for a particle
	 *//*
	public double evaluate(Particle particle) {
		double position[] = particle.getPosition();
		double fit = evaluate(position);
		particle.setFitness(fit, maximize);
		return fit;
	}

	*//**
	 * Is 'otherValue' better than 'fitness'?
	 * @param fitness
	 * @param otherValue
	 * @return true if 'otherValue' is better than 'fitness'
	 *//*
	public boolean isBetterThan(double fitness, double otherValue) {
		if (maximize) {
			if (otherValue > fitness) return true;
		} else {
			if (otherValue < fitness) return true;
		}
		return false;
	}

	*//** Are we maximizing this fitness function? *//*
	public boolean isMaximize() {
		return maximize;
	}

	public void setMaximize(boolean maximize) {
		this.maximize = maximize;
	}

}*/

/*

public class TetrisFitnessFunction extends FitnessFunction {

	Particle[] particles;
	HashMap<Integer, Integer> indexToFitness;

	public TetrisFitnessFunction() {
		super();
		indexToFitness = new HashMap<>();

	}

	public void setParticles(Particle[] particles) {
		this.particles = particles;
	}

	public void clearHashMap() {
		indexToFitness.clear();
	}

	public void set(int index, int clearedRows) {
		indexToFitness.put(index, clearedRows);
	}

	public int findParticleIndexFromPosition(double[] pos) {
		int index = -1;
		for (int i = 0; i < particles.length; i++) {
			double[] partPos = particles[i].getPosition();

			if (Arrays.equals(partPos, pos)) {
				return i;
			}
		}

		return index;
	}

	public double evaluate(double[] pos) {
		int index = findParticleIndexFromPosition(pos);

		return indexToFitness.get(index);
	}
}

// Start of swarm code

*//**
 * Basic (abstract) particle
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public abstract class Particle {

	*//** Best fitness function so far *//*
	double bestFitness;
	*//** Best particles's position so far *//*
	double bestPosition[];
	*//** current fitness *//*
	double fitness;
	*//** Position *//*
	double position[];
	*//** Velocity *//*
	double velocity[];

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------

	*//**
	 * Constructor 
	 *//*
	public Particle() {
		throw new RuntimeException("You probably need to implement your own 'Particle' class");
	}

	*//**
	 * Constructor 
	 * @param dimension : Particle's dimension
	 *//*
	public Particle(int dimension) {
		allocate(dimension);
	}

	*//**
	 * Constructor 
	 * @param sampleParticle : A sample particles to copy
	 *//*
	public Particle(Particle sampleParticle) {
		int dimension = sampleParticle.getDimension();
		allocate(dimension);
	}

	//-------------------------------------------------------------------------
	// Methods
	//-------------------------------------------------------------------------

	*//** Allocate memory *//*
	public void allocate(int dimension) {
		position = new double[dimension];
		bestPosition = new double[dimension];
		velocity = new double[dimension];
		bestFitness = Double.NaN;
		fitness = Double.NaN;
		for (int i = 0; i < position.length; i++)
			bestPosition[i] = Double.NaN;
	}

	*//**
	 * Apply position and velocity constraints (clamp)
	 * @param minPosition : Minimum position
	 * @param maxPosition : Maximum position
	 * @param minVelocity : Minimum velocity
	 * @param maxVelocity : Maximum velocity
	 *//*
	public void applyConstraints(double[] minPosition, double[] maxPosition, double[] minVelocity, double[] maxVelocity) {
		//---
		// Every constraint is set? (do all of them it one loop)
		//---
		if ((minPosition != null) && (maxPosition != null) && (minVelocity != null) && (maxVelocity != null)) for (int i = 0; i < position.length; i++) {
			if (!Double.isNaN(minPosition[i])) position[i] = (minPosition[i] > position[i] ? minPosition[i] : position[i]);
			if (!Double.isNaN(maxPosition[i])) position[i] = (maxPosition[i] < position[i] ? maxPosition[i] : position[i]);
			if (!Double.isNaN(minVelocity[i])) velocity[i] = (minVelocity[i] > velocity[i] ? minVelocity[i] : velocity[i]);
			if (!Double.isNaN(maxVelocity[i])) velocity[i] = (maxVelocity[i] < velocity[i] ? maxVelocity[i] : velocity[i]);
		}
		else {
			//---
			// Position constraints are set? (do both of them in the same loop)
			//---
			if ((minPosition != null) && (maxPosition != null)) for (int i = 0; i < position.length; i++) {
				if (!Double.isNaN(minPosition[i])) position[i] = (minPosition[i] > position[i] ? minPosition[i] : position[i]);
				if (!Double.isNaN(maxPosition[i])) position[i] = (maxPosition[i] < position[i] ? maxPosition[i] : position[i]);
			}
			else {
				//---
				// Do it individually
				//---
				if (minPosition != null) for (int i = 0; i < position.length; i++)
					if (!Double.isNaN(minPosition[i])) position[i] = (minPosition[i] > position[i] ? minPosition[i] : position[i]);
				if (maxPosition != null) for (int i = 0; i < position.length; i++)
					if (!Double.isNaN(maxPosition[i])) position[i] = (maxPosition[i] < position[i] ? maxPosition[i] : position[i]);
			}

			//---
			// Velocity constraints are set? (do both of them in the same loop)
			//---
			if ((minVelocity != null) && (maxVelocity != null)) for (int i = 0; i < velocity.length; i++) {
				if (!Double.isNaN(minVelocity[i])) velocity[i] = (minVelocity[i] > velocity[i] ? minVelocity[i] : velocity[i]);
				if (!Double.isNaN(maxVelocity[i])) velocity[i] = (maxVelocity[i] < velocity[i] ? maxVelocity[i] : velocity[i]);
			}
			else {
				//---
				// Do it individually
				//---
				if (minVelocity != null) for (int i = 0; i < velocity.length; i++)
					if (!Double.isNaN(minVelocity[i])) velocity[i] = (minVelocity[i] > velocity[i] ? minVelocity[i] : velocity[i]);
				if (maxVelocity != null) for (int i = 0; i < velocity.length; i++)
					if (!Double.isNaN(maxVelocity[i])) velocity[i] = (maxVelocity[i] < velocity[i] ? maxVelocity[i] : velocity[i]);
			}
		}
	}

	*//** Copy position[] to positionCopy[] *//*
	public void copyPosition(double positionCopy[]) {
		for (int i = 0; i < position.length; i++)
			positionCopy[i] = position[i];
	}

	*//** Copy position[] to bestPosition[] *//*
	public void copyPosition2Best() {
		for (int i = 0; i < position.length; i++)
			bestPosition[i] = position[i];
	}

	public double getBestFitness() {
		return bestFitness;
	}

	public double[] getBestPosition() {
		return bestPosition;
	}

	public int getDimension() {
		return position.length;
	}

	public double getFitness() {
		return fitness;
	}

	public double[] getPosition() {
		return position;
	}

	public double[] getVelocity() {
		return velocity;
	}

	*//**
	 * Initialize a particles's position and velocity vectors 
	 * @param maxPosition : Vector stating maximum position for each dimension
	 * @param minPosition : Vector stating minimum position for each dimension
	 * @param maxVelocity : Vector stating maximum velocity for each dimension
	 * @param minVelocity : Vector stating minimum velocity for each dimension
	 *//*
	public void init(double maxPosition[], double minPosition[], double maxVelocity[], double minVelocity[]) {
		for (int i = 0; i < position.length; i++) {
			if (Double.isNaN(maxPosition[i])) throw new RuntimeException("maxPosition[" + i + "] is NaN!");
			if (Double.isInfinite(maxPosition[i])) throw new RuntimeException("maxPosition[" + i + "] is Infinite!");

			if (Double.isNaN(minPosition[i])) throw new RuntimeException("minPosition[" + i + "] is NaN!");
			if (Double.isInfinite(minPosition[i])) throw new RuntimeException("minPosition[" + i + "] is Infinite!");

			if (Double.isNaN(maxVelocity[i])) throw new RuntimeException("maxVelocity[" + i + "] is NaN!");
			if (Double.isInfinite(maxVelocity[i])) throw new RuntimeException("maxVelocity[" + i + "] is Infinite!");

			if (Double.isNaN(minVelocity[i])) throw new RuntimeException("minVelocity[" + i + "] is NaN!");
			if (Double.isInfinite(minVelocity[i])) throw new RuntimeException("minVelocity[" + i + "] is Infinite!");

			// Initialize using uniform distribution
			position[i] = (maxPosition[i] - minPosition[i]) * Math.random() + minPosition[i];
			velocity[i] = (maxVelocity[i] - minVelocity[i]) * Math.random() + minVelocity[i];

			bestPosition[i] = Double.NaN;
		}
	}

	*//**
	 * Create a new instance of this particle 
	 * @return A new particle, just like this one
	 *//*
	public Object selfFactory() {
		Class cl = this.getClass();
		Constructor cons;

		try {
			cons = cl.getConstructor((Class[]) null);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}

		try {
			return cons.newInstance((Object[]) null);
		} catch (IllegalArgumentException e1) {
			throw new RuntimeException(e1);
		} catch (InstantiationException e1) {
			throw new RuntimeException(e1);
		} catch (IllegalAccessException e1) {
			throw new RuntimeException(e1);
		} catch (InvocationTargetException e1) {
			throw new RuntimeException(e1);
		}
	}

	public void setBestFitness(double bestFitness) {
		this.bestFitness = bestFitness;
	}

	public void setBestPosition(double[] bestPosition) {
		this.bestPosition = bestPosition;
	}

	*//**
	 * Set fitness and best fitness accordingly.
	 * If it's the best fitness so far, copy data to bestFitness[]
	 * @param fitness : New fitness value
	 * @param maximize : Are we maximizing or minimizing fitness function?
	 *//*
	public void setFitness(double fitness, boolean maximize) {
		this.fitness = fitness;
		if ((maximize && (fitness > bestFitness)) // Maximize and bigger? => store data
				|| (!maximize && (fitness < bestFitness)) // Minimize and smaller? => store data too
				|| Double.isNaN(bestFitness)) {
			copyPosition2Best();
			bestFitness = fitness;
		}
	}

	public void setPosition(double[] position) {
		this.position = position;
	}

	public void setVelocity(double[] velocity) {
		this.velocity = velocity;
	}

	*//** Printable string *//*
	@Override
	public String toString() {
		String str = "fitness: " + fitness + "\tbest fitness: " + bestFitness;

		if (position != null) {
			str += "\n\tPosition:\t";
			for (int i = 0; i < position.length; i++)
				str += position[i] + "\t";
		}

		if (velocity != null) {
			str += "\n\tVelocity:\t";
			for (int i = 0; i < velocity.length; i++)
				str += velocity[i] + "\t";
		}

		if (bestPosition != null) {
			str += "\n\tBest:\t";
			for (int i = 0; i < bestPosition.length; i++)
				str += bestPosition[i] + "\t";
		}

		str += "\n";
		return str;
	}
}

*//**
 * A neighborhood of particles
 * 
 * @author pcingola
 *//*
public abstract class Neighborhood {

	// All neighborhoods are stored here, so that we do not need to calculate them each time
	HashMap<Particle, Collection<Particle>> neighborhoods;
	// The best particle in the neighborhood is stored here
	HashMap<Particle, Particle> bestInNeighborhood;

	public Neighborhood() {
		neighborhoods = new HashMap<Particle, Collection<Particle>>();
		bestInNeighborhood = new HashMap<Particle, Particle>();
	}

	*//**
	 * Calculate all neighbors of particle 'p'
	 * 
	 * Note: The p's neighbors DO NOT include 'p'
	 * 
	 * @param p : a particle
	 * @return A collection with all neighbors
	 *//*
	public abstract Collection<Particle> calcNeighbours(Particle p);

	*//**
	 * Get the best particle in the neighborhood
	 * @param p
	 * @return The best particle in the neighborhood of 'p'
	 *//*
	public Particle getBestParticle(Particle p) {
		return bestInNeighborhood.get(p);
	}

	*//**
	 * Get the best position ever found by all the particles in the neighborhood of 'p'
	 * @param p
	 * @return The best position in the neighborhood of 'p'
	 *//*
	public double[] getBestPosition(Particle p) {
		Particle bestp = getBestParticle(p);
		if (bestp == null) return null;
		return bestp.getBestPosition();
	}

	*//**
	 * Get all neighbors of particle 'p'
	 * @param p : a particle
	 * @return A collection with all neighbors
	 *//*
	public Collection<Particle> getNeighbours(Particle p) {
		Collection<Particle> neighs = neighborhoods.get(p);
		if (neighs == null) neighs = calcNeighbours(p);
		return neighs;
	}

	*//**
	 * Initialize neighborhood
	 * @param swarm
	 * @return 
	 *//*
	public void init(Swarm swarm) {
		// Create neighborhoods for each particle
		for (Particle p : swarm) {
			Collection<Particle> neigh = getNeighbours(p);
			neighborhoods.put(p, neigh);
		}
	}

	*//**
	 * Update neighborhood: This is called after each iteration
	 * @param swarm
	 * @return 
	 *//*
	public void update(Swarm swarm, Particle p) {
		// Find best fitness in this neighborhood
		Particle pbest = getBestParticle(p);
		if ((pbest == null) || swarm.getFitnessFunction().isBetterThan(pbest.getBestFitness(), p.getBestFitness())) {
			// Particle 'p' is the new 'best in neighborhood' => we need to update all neighbors
			Collection<Particle> neigh = getNeighbours(p);
			for (Particle pp : neigh) {
				bestInNeighborhood.put(pp, p);
			}
		}
	}
}

*//**
 * A neighborhood of particles
 * 
 * @author pcingola
 *//*
public class Neighborhood1D extends Neighborhood {

	int size;
	boolean circular;
	ArrayList<Particle> array1d;

	*//**
	 * Create a 1 dimensional neighborhood (all particles have 2 neighbors: 1 to the left, 1 to the right)
	 * @param size : How many particles to each side do we consider? (total neighborhood is 2*size)
	 * @param circular : If true, the first particle and the last particles are neighbors
	 *//*
	public Neighborhood1D(int size, boolean circular) {
		super();
		this.size = size;
		this.circular = circular;
		array1d = new ArrayList<Particle>();
	}

	@Override
	public Collection<Particle> calcNeighbours(Particle p) {
		ArrayList<Particle> neigh = new ArrayList<Particle>();
		int idx = findIndex(p); // Find this particle's index

		// Add all the particles in the neighborhood
		for (int i = idx - size; i <= (idx + size); i++) {
			Particle pp = getParticle(i);
			if ((pp != null) && (pp != p)) neigh.add(pp); // Do not add 'p'
		}

		return neigh;
	}

	*//**
	 * Find a particle's number
	 * @param p
	 * @return
	 *//*
	int findIndex(Particle p) {
		for (int i = 0; i < array1d.size(); i++) {
			if (p == array1d.get(i)) return i;
		}
		throw new RuntimeException("Cannot find particle. This should never happen!\n" + p);
	}

	*//**
	 * Get particle number 'idx'
	 * @param idx
	 * @return
	 *//*
	Particle getParticle(int idx) {
		int arraySize = array1d.size();
		if ((idx >= 0) && (idx < array1d.size())) return array1d.get(idx); // Within limits => OK
		if (!circular) return null; // Not circular? => Nothing to do

		if (idx >= arraySize) idx = idx % arraySize;
		else if (idx < 0) idx += arraySize; // This might not work if 'size' > 'arraySize'

		return array1d.get(idx);
	}

	@Override
	public void init(Swarm swarm) {
		// Add all particles to the array
		for (Particle p : swarm)
			array1d.add(p);

		super.init(swarm); // Call to Neighborhood.init() method
	}
}

*//**
 * A swarm of particles
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public static class Swarm implements Iterable<Particle> {

	public static double DEFAULT_GLOBAL_INCREMENT = 0.9;
	public static double DEFAULT_INERTIA = 1;
	public static int DEFAULT_NUMBER_OF_PARTICLES = 25;
	public static double DEFAULT_PARTICLE_INCREMENT = 0.9;
	public static double VELOCITY_GRAPH_FACTOR = 10.0;

	*//** Best fitness so far (global best) *//*
	double bestFitness;
	*//** Index of best particle so far *//*
	int bestParticleIndex;
	*//** Best position so far (global best) *//*
	double bestPosition[];
	*//** Fitness function for this swarm *//*
	TetrisFitnessFunction fitnessFunction;
	*//**
	 * Global increment (for velocity update), usually called 'c2' constant
	 *//*
	double globalIncrement;
	*//** Inertia (for velocity update), usually called 'w' constant *//*
	double inertia;
	*//** Maximum position (for each dimension) *//*
	double maxPosition[];
	*//** Maximum Velocity (for each dimension) *//*
	double maxVelocity[];
	*//** Minimum position (for each dimension) *//*
	double minPosition[];
	*//**
	 * Minimum Velocity for each dimension. WARNING: Velocity is no in Abs
	 * value (so setting minVelocity to 0 is NOT correct!)
	 *//*
	double minVelocity[];
	*//** How many times 'particle.evaluate()' has been called? *//*
	int numberOfEvaliations;
	*//** Number of particles in this swarm *//*
	int numberOfParticles;
	*//**
	 * Particle's increment (for velocity update), usually called 'c1'
	 * constant
	 *//*
	double particleIncrement;
	*//** Particles in this swarm *//*
	Particle particles[];
	*//** Particle update strategy *//*
	ParticleUpdate particleUpdate;
	*//** A sample particles: Build other particles based on this one *//*
	Particle sampleParticle;
	*//** Variables update *//*
	VariablesUpdate variablesUpdate;
	*//** Neighborhood *//*
	@SuppressWarnings("unchecked")
	Neighborhood neighborhood;
	*//**
	 * Neighborhood increment (for velocity update), usually called 'c3'
	 * constant
	 *//*
	double neighborhoodIncrement;
	*//** A collection used for 'Iterable' interface *//*
	ArrayList<Particle> particlesList;

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	*//**
	 * Create a Swarm and set default values
	 * 
	 * @param numberOfParticles
	 *            : Number of particles in this swarm (should be greater
	 *            than 0). If unsure about this parameter, try
	 *            Swarm.DEFAULT_NUMBER_OF_PARTICLES or greater
	 * @param sampleParticle
	 *            : A particle that is a sample to build all other particles
	 * @param fitnessFunction
	 *            : Fitness function used to evaluate each particle
	 *//*
	public Swarm(int numberOfParticles, Particle sampleParticle, TetrisFitnessFunction fitnessFunction) {
		if (sampleParticle == null)
			throw new RuntimeException("Sample particle can't be null!");
		if (numberOfParticles <= 0)
			throw new RuntimeException("Number of particles should be greater than zero.");

		globalIncrement = DEFAULT_GLOBAL_INCREMENT;
		inertia = DEFAULT_INERTIA;
		particleIncrement = DEFAULT_PARTICLE_INCREMENT;
		numberOfEvaliations = 0;
		this.numberOfParticles = numberOfParticles;
		this.sampleParticle = sampleParticle;
		this.fitnessFunction = fitnessFunction;
		bestFitness = Double.NaN;
		bestParticleIndex = -1;

		// Set up particle update strategy (default: ParticleUpdateSimple)
		particleUpdate = new ParticleUpdateSimple(sampleParticle);

		// Set up variablesUpdate strategy (default: VariablesUpdate)
		variablesUpdate = new VariablesUpdate();

		neighborhood = null;
		neighborhoodIncrement = 0.0;
		particlesList = null;
	}

	// -------------------------------------------------------------------------
	// Methods
	// -------------------------------------------------------------------------

	*//**
	 * Evaluate fitness function for every particle Warning: particles[]
	 * must be initialized and fitnessFunction must be set
	 *//*
	public void evaluate() {
		if (particles == null)
			throw new RuntimeException("No particles in this swarm! May be you need to call Swarm.init() method");
		if (fitnessFunction == null)
			throw new RuntimeException(
					"No fitness function in this swarm! May be you need to call Swarm.setFitnessFunction() method");

		// Initialize
		if (Double.isNaN(bestFitness)) {
			bestFitness = (fitnessFunction.isMaximize() ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			bestParticleIndex = -1;
		}

		// ---
		// Evaluate each particle (and find the 'best' one)
		// ---
		for (int i = 0; i < particles.length; i++) {
			// Evaluate particle
			double fit = fitnessFunction.evaluate(particles[i].getPosition());

			numberOfEvaliations++; // Update counter

			// Update 'best global' position
			if (fitnessFunction.isBetterThan(bestFitness, fit)) {
				bestFitness = fit; // Copy best fitness, index, and position
									// vector
				bestParticleIndex = i;
				if (bestPosition == null)
					bestPosition = new double[sampleParticle.getDimension()];
				particles[bestParticleIndex].copyPosition(bestPosition);
			}

			// Update 'best neighborhood'
			if (neighborhood != null) {
				neighborhood.update(this, particles[i]);
			}

		}
	}

	*//**
	 * Make an iteration: - evaluates the swarm - updates positions and
	 * velocities - applies positions and velocities constraints
	 *//*
	public void evolve() {
		// Initialize (if not already done)
		if (particles == null)
			init();

		evaluate(); // Evaluate particles
		update(); // Update positions and velocities

		variablesUpdate.update(this);
	}

	public double getBestFitness() {
		return bestFitness;
	}

	public Particle getBestParticle() {
		return particles[bestParticleIndex];
	}

	public int getBestParticleIndex() {
		return bestParticleIndex;
	}

	public double[] getBestPosition() {
		return bestPosition;
	}

	public FitnessFunction getFitnessFunction() {
		return fitnessFunction;
	}

	public double getGlobalIncrement() {
		return globalIncrement;
	}

	public double getInertia() {
		return inertia;
	}

	public double[] getMaxPosition() {
		return maxPosition;
	}

	public double[] getMaxVelocity() {
		return maxVelocity;
	}

	public double[] getMinPosition() {
		return minPosition;
	}

	public double[] getMinVelocity() {
		return minVelocity;
	}

	@SuppressWarnings("unchecked")
	public Neighborhood getNeighborhood() {
		return neighborhood;
	}

	*//**
	 * Return the best position in the neighborhood Note: If neighborhood is
	 * not defined (i.e. neighborhood is null) then 'particle' is returned
	 * so that it doesn't influence in particle update.
	 * 
	 * @param particle
	 * @return
	 *//*
	@SuppressWarnings("unchecked")
	public double[] getNeighborhoodBestPosition(Particle particle) {
		if (neighborhood == null)
			return particle.getPosition();
		double d[] = neighborhood.getBestPosition(particle);
		if (d == null)
			return particle.getPosition();
		return d;
	}

	public double getNeighborhoodIncrement() {
		return neighborhoodIncrement;
	}

	public int getNumberOfEvaliations() {
		return numberOfEvaliations;
	}

	public int getNumberOfParticles() {
		return numberOfParticles;
	}

	public Particle getParticle(int i) {
		return particles[i];
	}

	public double getParticleIncrement() {
		return particleIncrement;
	}

	public Particle[] getParticles() {
		return particles;
	}

	public ParticleUpdate getParticleUpdate() {
		return particleUpdate;
	}

	public Particle getSampleParticle() {
		return sampleParticle;
	}

	public VariablesUpdate getVariablesUpdate() {
		return variablesUpdate;
	}

	*//**
	 * Initialize every particle Warning: maxPosition[], minPosition[],
	 * maxVelocity[], minVelocity[] must be initialized and setted
	 *//*
	public void init() {
		// Init particles
		particles = new Particle[numberOfParticles];

		// Check constraints (they will be used to initialize particles)
		if (maxPosition == null)
			throw new RuntimeException("maxPosition array is null!");
		if (minPosition == null)
			throw new RuntimeException("maxPosition array is null!");
		if (maxVelocity == null) {
			// Default maxVelocity[]
			int dim = sampleParticle.getDimension();
			maxVelocity = new double[dim];
			for (int i = 0; i < dim; i++)
				maxVelocity[i] = (maxPosition[i] - minPosition[i]) / 2.0;
		}
		if (minVelocity == null) {
			// Default minVelocity[]
			int dim = sampleParticle.getDimension();
			minVelocity = new double[dim];
			for (int i = 0; i < dim; i++)
				minVelocity[i] = -maxVelocity[i];
		}

		// Init each particle
		for (int i = 0; i < numberOfParticles; i++) {
			particles[i] = (Particle) sampleParticle.selfFactory(); // Create
																	// a new
																	// particles
																	// (using
																	// 'sampleParticle'
																	// as
																	// reference)
			particles[i].init(maxPosition, minPosition, maxVelocity, minVelocity); // Initialize
																					// it
		}

		// Init neighborhood
		if (neighborhood != null)
			neighborhood.init(this);
	}

	*//**
	 * Iterate over all particles
	 *//*
	public Iterator<Particle> iterator() {
		if (particlesList == null) {
			particlesList = new ArrayList<Particle>(particles.length);
			for (int i = 0; i < particles.length; i++)
				particlesList.add(particles[i]);
		}

		return particlesList.iterator();
	}

	public void setBestParticleIndex(int bestParticle) {
		bestParticleIndex = bestParticle;
	}

	public void setBestPosition(double[] bestPosition) {
		this.bestPosition = bestPosition;
	}

	public void setFitnessFunction(TetrisFitnessFunction fitnessFunction) {
		this.fitnessFunction = fitnessFunction;
	}

	public void setGlobalIncrement(double globalIncrement) {
		this.globalIncrement = globalIncrement;
	}

	public void setInertia(double inertia) {
		this.inertia = inertia;
	}

	*//**
	 * Sets every maxVelocity[] and minVelocity[] to 'maxVelocity' and
	 * '-maxVelocity' respectively
	 * 
	 * @param maxVelocity
	 *//*
	public void setMaxMinVelocity(double maxVelocity) {
		if (sampleParticle == null)
			throw new RuntimeException(
					"Need to set sample particle before calling this method (use Swarm.setSampleParticle() method)");
		int dim = sampleParticle.getDimension();
		this.maxVelocity = new double[dim];
		minVelocity = new double[dim];
		for (int i = 0; i < dim; i++) {
			this.maxVelocity[i] = maxVelocity;
			minVelocity[i] = -maxVelocity;
		}
	}

	*//**
	 * Sets every maxPosition[] to 'maxPosition'
	 * 
	 * @param maxPosition
	 *//*
	public void setMaxPosition(double maxPosition) {
		if (sampleParticle == null)
			throw new RuntimeException(
					"Need to set sample particle before calling this method (use Swarm.setSampleParticle() method)");
		int dim = sampleParticle.getDimension();
		this.maxPosition = new double[dim];
		for (int i = 0; i < dim; i++)
			this.maxPosition[i] = maxPosition;
	}

	public void setMaxPosition(double[] maxPosition) {
		this.maxPosition = maxPosition;
	}

	public void setMaxVelocity(double[] maxVelocity) {
		this.maxVelocity = maxVelocity;
	}

	*//**
	 * Sets every minPosition[] to 'minPosition'
	 * 
	 * @param minPosition
	 *//*
	public void setMinPosition(double minPosition) {
		if (sampleParticle == null)
			throw new RuntimeException(
					"Need to set sample particle before calling this method (use Swarm.setSampleParticle() method)");
		int dim = sampleParticle.getDimension();
		this.minPosition = new double[dim];
		for (int i = 0; i < dim; i++)
			this.minPosition[i] = minPosition;
	}

	public void setMinPosition(double[] minPosition) {
		this.minPosition = minPosition;
	}

	public void setMinVelocity(double minVelocity[]) {
		this.minVelocity = minVelocity;
	}

	@SuppressWarnings("unchecked")
	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
	}

	public void setNeighborhoodIncrement(double neighborhoodIncrement) {
		this.neighborhoodIncrement = neighborhoodIncrement;
	}

	public void setNumberOfEvaliations(int numberOfEvaliations) {
		this.numberOfEvaliations = numberOfEvaliations;
	}

	public void setNumberOfParticles(int numberOfParticles) {
		this.numberOfParticles = numberOfParticles;
	}

	public void setParticleIncrement(double particleIncrement) {
		this.particleIncrement = particleIncrement;
	}

	public void setParticles(Particle[] particle) {
		particles = particle;
		particlesList = null;
	}

	public void setParticleUpdate(ParticleUpdate particleUpdate) {
		this.particleUpdate = particleUpdate;
	}

	public void setSampleParticle(Particle sampleParticle) {
		this.sampleParticle = sampleParticle;
	}

	public void setVariablesUpdate(VariablesUpdate variablesUpdate) {
		this.variablesUpdate = variablesUpdate;
	}

	*//**
	 * Show a swarm in a graph
	 * 
	 * @param graphics
	 *            : Grapics object
	 * @param foreground
	 *            : foreground color
	 * @param width
	 *            : graphic's width
	 * @param height
	 *            : graphic's height
	 * @param dim0
	 *            : Dimention to show ('x' axis)
	 * @param dim1
	 *            : Dimention to show ('y' axis)
	 * @param showVelocity
	 *            : Show velocity tails?
	 *//*
	public void show(Graphics graphics, Color foreground, int width, int height, int dim0, int dim1,
			boolean showVelocity) {
		graphics.setColor(foreground);

		if (particles != null) {
			double scalePosW = width / (maxPosition[dim0] - minPosition[dim0]);
			double scalePosH = height / (maxPosition[dim1] - minPosition[dim1]);
			double minPosW = minPosition[dim0];
			double minPosH = minPosition[dim1];

			double scaleVelW = width / (VELOCITY_GRAPH_FACTOR * (maxVelocity[dim0] - minVelocity[dim0]));
			double scaleVelH = height / (VELOCITY_GRAPH_FACTOR * (maxVelocity[dim1] - minVelocity[dim1]));
			double minVelW = minVelocity[dim0] + (maxVelocity[dim0] - minVelocity[dim0]) / 2;
			double minVelH = minVelocity[dim1] + (maxVelocity[dim1] - minVelocity[dim1]) / 2;

			for (int i = 0; i < particles.length; i++) {
				int vx, vy, x, y;
				double pos[] = particles[i].getPosition();
				double vel[] = particles[i].getVelocity();
				x = (int) (scalePosW * (pos[dim0] - minPosW));
				y = height - (int) (scalePosH * (pos[dim1] - minPosH));
				graphics.drawRect(x - 1, y - 1, 3, 3);
				if (showVelocity) {
					vx = (int) (scaleVelW * (vel[dim0] - minVelW));
					vy = (int) (scaleVelH * (vel[dim1] - minVelH));
					graphics.drawLine(x, y, x + vx, y + vy);
				}
			}
		}
	}

	*//** Swarm size (number of particles) *//*
	public int size() {
		return particles.length;
	}

	*//** Printable string *//*
	@Override
	public String toString() {
		String str = "";

		if (particles != null)
			str += "Swarm size: " + particles.length + "\n";

		if ((minPosition != null) && (maxPosition != null)) {
			str += "Position ranges:\t";
			for (int i = 0; i < maxPosition.length; i++)
				str += "[" + minPosition[i] + ", " + maxPosition[i] + "]\t";
		}

		if ((minVelocity != null) && (maxVelocity != null)) {
			str += "\nVelocity ranges:\t";
			for (int i = 0; i < maxVelocity.length; i++)
				str += "[" + minVelocity[i] + ", " + maxVelocity[i] + "]\t";
		}

		if (sampleParticle != null)
			str += "\nSample particle: " + sampleParticle;

		if (particles != null) {
			str += "\nParticles:";
			for (int i = 0; i < particles.length; i++) {
				str += "\n\tParticle: " + i + "\t";
				str += particles[i].toString();
			}
		}
		str += "\n";

		return str;
	}

	*//**
	 * Return a string with some (very basic) statistics
	 * 
	 * @return A string
	 *//*
	public String toStringStats() {
		String stats = "";
		if (!Double.isNaN(bestFitness)) {
			stats += "Best fitness: " + bestFitness + "\nBest position: \t[";
			for (int i = 0; i < bestPosition.length; i++)
				stats += bestPosition[i] + (i < (bestPosition.length - 1) ? ", " : "");
			stats += "]\nNumber of evaluations: " + numberOfEvaliations + "\n";
		}
		return stats;
	}

	*//**
	 * Update every particle's position and velocity, also apply position
	 * and velocity constraints (if any) Warning: Particles must be already
	 * evaluated
	 *//*
	public void update() {
		// Initialize a particle update iteration
		particleUpdate.begin(this);

		// For each particle...
		for (int i = 0; i < particles.length; i++) {
			// Update particle's position and speed
			particleUpdate.update(this, particles[i]);

			// Apply position and velocity constraints
			particles[i].applyConstraints(minPosition, maxPosition, minVelocity, maxVelocity);
		}

		// Finish a particle update iteration
		particleUpdate.end(this);
	}
}

*//**
 * A swarm of repulsive particles
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public static class SwarmRepulsive extends Swarm {

	public static double DEFAULT_OTHER_PARTICLE_INCREMENT = 0.9;
	public static double DEFAULT_RANDOM_INCREMENT = 0.1;

	*//** Other particle increment *//*
	double otherParticleIncrement;
	*//** Random increment *//*
	double randomIncrement;

	*//**
	 * Create a Swarm and set default values
	 * 
	 * @param numberOfParticles
	 *            : Number of particles in this swarm (should be greater
	 *            than 0). If unsure about this parameter, try
	 *            Swarm.DEFAULT_NUMBER_OF_PARTICLES or greater
	 * @param sampleParticle
	 *            : A particle that is a sample to build all other particles
	 * @param fitnessFunction
	 *            : Fitness function used to evaluate each particle
	 *//*
	public SwarmRepulsive(int numberOfParticles, Particle sampleParticle, TetrisFitnessFunction fitnessFunction) {
		super(numberOfParticles, sampleParticle, fitnessFunction);

		this.otherParticleIncrement = DEFAULT_OTHER_PARTICLE_INCREMENT;
		this.randomIncrement = DEFAULT_RANDOM_INCREMENT;

		// Set up particle update strategy (default:
		// ParticleUpdateRepulsive)
		this.particleUpdate = new ParticleUpdateRepulsive(sampleParticle);
	}

	public double getOtherParticleIncrement() {
		return otherParticleIncrement;
	}

	public double getRandomIncrement() {
		return randomIncrement;
	}

	public void setOtherParticleIncrement(double otherParticleIncrement) {
		this.otherParticleIncrement = otherParticleIncrement;
	}

	public void setRandomIncrement(double randomIncrement) {
		this.randomIncrement = randomIncrement;
	}
}

*//**
 * Swarm variables update Every Swarm.evolve() iteration, update() is called
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public static class VariablesUpdate {

	*//**
	 * Default constructor
	 *//*
	public VariablesUpdate() {
		super();
	}

	*//**
	 * Update Swarm parameters here
	 * 
	 * @param swarm
	 *            : Swarm to update
	 *//*
	public void update(Swarm swarm) {
		swarm.setInertia(0.995 * swarm.getInertia());
	}
}

*//**
 * Particle update strategy
 * 
 * Every Swarm.evolve() itereation the following methods are called -
 * begin(Swarm) : Once at the begining of each iteration -
 * update(Swarm,Particle) : Once for each particle - end(Swarm) : Once at
 * the end of each iteration
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public abstract class ParticleUpdate {

	*//**
	 * Constructor
	 * 
	 * @param particle
	 *            : Sample of particles that will be updated later
	 *//*
	public ParticleUpdate(Particle particle) {
	}

	*//**
	 * This method is called at the begining of each iteration Initialize
	 * random vectors use for local and global updates (rlocal[] and
	 * rother[])
	 *//*
	public void begin(Swarm swarm) {
	}

	*//** This method is called at the end of each iteration *//*
	public void end(Swarm swarm) {
	}

	*//** Update particle's velocity and position *//*
	public abstract void update(Swarm swarm, Particle particle);
}

*//**
 * Particle update strategy
 * 
 * Every Swarm.evolve() itereation the following methods are called -
 * begin(Swarm) : Once at the begining of each iteration -
 * update(Swarm,Particle) : Once for each particle - end(Swarm) : Once at
 * the end of each iteration
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public class ParticleUpdateSimple extends ParticleUpdate {

	*//** Random vector for local update *//*
	double rlocal[];
	*//** Random vector for global update *//*
	double rglobal[];
	*//** Random vector for neighborhood update *//*
	double rneighborhood[];

	*//**
	 * Constructor
	 * 
	 * @param particle
	 *            : Sample of particles that will be updated later
	 *//*
	public ParticleUpdateSimple(Particle particle) {
		super(particle);
		rlocal = new double[particle.getDimension()];
		rglobal = new double[particle.getDimension()];
		rneighborhood = new double[particle.getDimension()];
	}

	*//**
	 * This method is called at the begining of each iteration Initialize
	 * random vectors use for local and global updates (rlocal[] and
	 * rother[])
	 *//*
	@Override
	public void begin(Swarm swarm) {
		int i, dim = swarm.getSampleParticle().getDimension();
		for (i = 0; i < dim; i++) {
			rlocal[i] = Math.random();
			rglobal[i] = Math.random();
			rneighborhood[i] = Math.random();
		}
	}

	*//** This method is called at the end of each iteration *//*
	@Override
	public void end(Swarm swarm) {
	}

	*//** Update particle's velocity and position *//*
	@Override
	public void update(Swarm swarm, Particle particle) {

		double position[] = particle.getPosition();
		double velocity[] = particle.getVelocity();
		double globalBestPosition[] = swarm.getBestPosition();
		double particleBestPosition[] = particle.getBestPosition();
		double neighBestPosition[] = swarm.getNeighborhoodBestPosition(particle);

		if (particle.getFitness() * 5 >= swarm.getBestFitness()) {

			// Update velocity and position
			for (int i = 0; i < position.length; i++) {
				// Update velocity
				velocity[i] = swarm.getInertia() * velocity[i] // Inertia
						+ rlocal[i] * swarm.getParticleIncrement() * (particleBestPosition[i] - position[i]) // Local
																												// best
						+ rneighborhood[i] * swarm.getNeighborhoodIncrement() * (neighBestPosition[i] - position[i]) // Neighborhood
																														// best
						+ rglobal[i] * swarm.getGlobalIncrement() * (globalBestPosition[i] - position[i]); // Global
																											// best
				// Update position
				position[i] += velocity[i];
			}
		} else {
			for (int i = 0; i < position.length; i++) {
				double r = Math.random();
				double s = Math.random();
				double t = Math.random();
				if (r > 0.2) {
					if (s > 0.6) {
						position[i] = globalBestPosition[i];
					} else {
						position[i] = t * 20 - 10;
					}
				}
			}
		}

	}

	public void randomRestart() {

	}
}

*//**
 * Particle update: Fully random approach Note that rlocal and rother are
 * randomly choosen for each particle and for each dimention
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public class ParticleUpdateFullyRandom extends ParticleUpdate {

	*//**
	 * Constructor
	 * 
	 * @param particle
	 *            : Sample of particles that will be updated later
	 *//*
	public ParticleUpdateFullyRandom(Particle particle) {
		super(particle);
	}

	*//** Update particle's velocity and position *//*
	@Override
	public void update(Swarm swarm, Particle particle) {
		double position[] = particle.getPosition();
		double velocity[] = particle.getVelocity();
		double globalBestPosition[] = swarm.getBestPosition();
		double particleBestPosition[] = particle.getBestPosition();
		double neighBestPosition[] = swarm.getNeighborhoodBestPosition(particle);

		// Update velocity and position
		for (int i = 0; i < position.length; i++) {
			// Update position
			position[i] = position[i] + velocity[i];

			// Update velocity
			velocity[i] = swarm.getInertia() * velocity[i] // Inertia
					+ Math.random() * swarm.getParticleIncrement() * (particleBestPosition[i] - position[i]) // Local
																												// best
					+ Math.random() * swarm.getNeighborhoodIncrement() * (neighBestPosition[i] - position[i]) // Neighborhood
																												// best
					+ Math.random() * swarm.getGlobalIncrement() * (globalBestPosition[i] - position[i]); // Global
																											// best
		}
	}
}

*//**
 * Particle update: Each particle selects an rlocal and rother independently
 * from other particles' values
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public class ParticleUpdateRandomByParticle extends ParticleUpdate {

	*//**
	 * Constructor
	 * 
	 * @param particle
	 *            : Sample of particles that will be updated later
	 *//*
	public ParticleUpdateRandomByParticle(Particle particle) {
		super(particle);
	}

	*//** Update particle's velocity and position *//*
	@Override
	public void update(Swarm swarm, Particle particle) {
		double position[] = particle.getPosition();
		double velocity[] = particle.getVelocity();
		double globalBestPosition[] = swarm.getBestPosition();
		double particleBestPosition[] = particle.getBestPosition();
		double neighBestPosition[] = swarm.getNeighborhoodBestPosition(particle);

		double rlocal = Math.random();
		double rneighborhood = Math.random();
		double rglobal = Math.random();

		// Update velocity and position
		for (int i = 0; i < position.length; i++) {
			// Update position
			position[i] = position[i] + velocity[i];

			// Update velocity
			velocity[i] = swarm.getInertia() * velocity[i] // Inertia
					+ rlocal * swarm.getParticleIncrement() * (particleBestPosition[i] - position[i]) // Local
																										// best
					+ rneighborhood * swarm.getNeighborhoodIncrement() * (neighBestPosition[i] - position[i]) // Neighborhood
																												// best
					+ rglobal * swarm.getGlobalIncrement() * (globalBestPosition[i] - position[i]); // Global
																									// best
		}
	}
}

*//**
 * Particle update strategy Warning: It's designed to be used with
 * SwarmRepulsive swarms
 * 
 * @author Pablo Cingolani <pcingola@users.sourceforge.net>
 *//*
public class ParticleUpdateRepulsive extends ParticleUpdate {

	*//** Random vector for local update *//*
	double rlocal[];
	*//** Random vector for global update *//*
	double rother[];
	*//** Random vector for neighborhood update *//*
	double rneighborhood[];
	*//** Random factor for random velocity update *//*
	double randRand;

	*//**
	 * Constructor
	 * 
	 * @param particle
	 *            : Sample of particles that will be updated later
	 *//*
	public ParticleUpdateRepulsive(Particle particle) {
		super(particle);
		rlocal = new double[particle.getDimension()];
		rother = new double[particle.getDimension()];
		rneighborhood = new double[particle.getDimension()];
	}

	*//**
	 * This method is called at the begining of each iteration Initialize
	 * random vectors use for local and global updates (rlocal[] and
	 * rother[])
	 *//*
	@Override
	public void begin(Swarm swarm) {
		randRand = Math.random();// Random factor for random velocity

		int i, dim = swarm.getSampleParticle().getDimension();
		for (i = 0; i < dim; i++) {
			rlocal[i] = Math.random();
			rother[i] = Math.random();
			rneighborhood[i] = Math.random();
		}
	}

	*//**
	 * Update particle's position and velocity using repulsive algorithm
	 *//*
	@Override
	public void update(Swarm swarm, Particle particle) {
		double position[] = particle.getPosition();
		double velocity[] = particle.getVelocity();
		double particleBestPosition[] = particle.getBestPosition();
		double maxVelocity[] = swarm.getMaxVelocity();
		double minVelocity[] = swarm.getMinVelocity();
		SwarmRepulsive swarmRepulsive = (SwarmRepulsive) swarm;

		// Randomly select other particle
		int randOtherParticle = (int) (Math.random() * swarm.size());
		double otherParticleBestPosition[] = swarm.getParticle(randOtherParticle).getBestPosition();
		double neighBestPosition[] = swarm.getNeighborhoodBestPosition(particle);

		// Update velocity and position
		for (int i = 0; i < position.length; i++) {
			// Update position
			position[i] = position[i] + velocity[i];

			// Create a random velocity (one on every dimention)
			double randVelocity = velocity[i] = (maxVelocity[i] - minVelocity[i]) * Math.random() + minVelocity[i];

			// Update velocity
			velocity[i] = swarmRepulsive.getInertia() * velocity[i] // Inertia
					+ rlocal[i] * swarmRepulsive.getParticleIncrement() * (particleBestPosition[i] - position[i]) // Local
																													// best
					+ rneighborhood[i] * swarm.getNeighborhoodIncrement() * (neighBestPosition[i] - position[i]) // Neighborhood
																													// best
					+ rother[i] * swarmRepulsive.getOtherParticleIncrement()
							* (otherParticleBestPosition[i] - position[i]) // other
																			// Particle
																			// Best
																			// Position
					+ randRand * swarmRepulsive.getRandomIncrement() * randVelocity; // Random
																						// velocity
		}
	}
}
*/