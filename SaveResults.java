import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class SaveResults {

	public static void saveSate(int iter, String data) {
		try {
			File f = new File("ExperimentalResults.txt");
			PrintWriter pw = new PrintWriter(new FileWriter(f,true));
			pw.println("Iteration: " + iter);
			pw.println(data);
			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}