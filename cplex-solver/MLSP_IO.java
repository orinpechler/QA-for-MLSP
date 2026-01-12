import java.io.*;
import java.util.*;

/* This program implements the MILP for the Multi-League Sports Scheduling problem and solves it.
 * For this the MILP_Instance.java file is used.
 */

public class MLSP_IO {
	
	public static void main(String args[]) {
		
		Scanner input = new Scanner(System.in);
		System.out.println("From what file should the data be read?");

		String fileIN = input.next();
		
		System.out.println("The solution is outputted to file: CPLEX-Sol-" + fileIN);
		String fileOUT = "CPLEX-Sol-" + fileIN;
			
		input.close();
			
		try {
			MLSP_Instance instance = new MLSP_Instance(fileIN);
			instance.solveMILPforMLSP();
			instance.outputSolution(fileOUT);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
