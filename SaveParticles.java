import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class SaveParticles {

	public static void saveSate(int iter, Particle p[]) {
		try {
			File f = new File("particlesValue.txt");
			PrintWriter pw = new PrintWriter(f);
			pw.println("iteration " + iter);
			String particleString = null;
			for (int i = 0; i < p.length; i++) {
				particleString = arrayToString(p[i].getPosition());
				particleString = particleString.concat(" ").concat(
						arrayToString(p[i].getBestPosition()));
				particleString = particleString.concat(" ").concat(
						arrayToString(p[i].getVelocity()));
				particleString = particleString.concat(" ").concat(
						Double.toString(p[i].getFitness()));
				particleString = particleString.concat(" ").concat(
						Double.toString(p[i].getBestFitness()));
				pw.println(particleString);
			}
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static int loadState(Particle[] p) {
		int r =0;
		File f = new File("particlesValue.txt");
		try {
			Scanner sc = new Scanner(f);
			if (sc.hasNextLine()) {
				String itre = sc.nextLine();
				System.out.println("starting from " + itre);
				String [] t = itre.split(" ");
				r = Integer.parseInt(t[1]);
			}
			int count = 0;
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				String[] contents = line.split(" ");
				p[count].setPosition(stringToArray(contents[0]));
				p[count].setBestPosition(stringToArray(contents[1]));
				p[count].setVelocity(stringToArray(contents[2]));
				p[count].setFitness((Double.parseDouble(contents[3])), false);
				p[count].setBestFitness(Double.parseDouble(contents[4]));
				count++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	public static double[] stringToArray(String s) {
		double[] d = new double[6];
		String[] str = s.split(",");
		for (int i = 0; i < 6; i++) {
			d[i] = Double.parseDouble(str[i]);
		}
		return d;
	}

	public static String arrayToString(double[] d) {
		String str = "";
		for (int i = 0; i < d.length; i++) {
			str = str.concat(Double.toString(d[i])).concat(",");
		}
		return str;
	}
}
