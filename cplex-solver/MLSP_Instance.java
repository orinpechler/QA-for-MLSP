import ilog.concert.*;
import ilog.cplex.*;
import java.io.*;
import java.util.*;

/* This program creates an instance for the MLSP from a data file, and solves it using the MILP
 * as defined in the paper.
 */

public class MLSP_Instance {
	
	// Everything necessary for the MILP, which will be read from the data file
	int numTeams;
	int numLeagues;
	int numClubs;
	int numRounds;
	int leagueSize;
	ArrayList<int[]> clubs;		// Clubs are stored as an array list as the number of teams per club varies
	int[] clubSizes;
	int[] clubCapacities;
	int[][] leagues;
	int[][] U;
	
	// These variables will store the solutions obtained from the MILP
	double numViolations;
	double[][] z;
	double[][] x;
	
	double runningTime;
	
	/**
	 * This creates an instance for the MLSP from a data file
	 **/
	public MLSP_Instance(String filename) throws FileNotFoundException {
		
		// This sets the working directory to the folder where the problem instances of the Data Generator are stored
		String newWD = "../data";
		File file = new File(newWD, filename);
		Scanner input = new Scanner(file);
		
		this.numTeams = input.nextInt();
		this.numLeagues = input.nextInt();
		this.numClubs = input.nextInt();
		this.leagueSize = input.nextInt();
		this.numRounds = 2*(leagueSize - 1);
		
		this.clubs = new ArrayList<int[]>();
		this.clubSizes = new int[numClubs];
		this.clubCapacities = new int[numClubs];
		this.leagues = new int[numLeagues][leagueSize];
		this.U = new int[leagueSize][numRounds];
		
		this.z = new double[numClubs][numRounds];
		this.x = new double[numTeams][leagueSize];
 		
		// The numbering of teams is corrected to take on a value from 0 to (numTeams - 1)
		// Clubs are stored separately as an array, but as the number of teams per club differ over all clubs,
		// each array is stored in an array list. 
		for(int i = 0; i < this.numClubs; i++) {
			int clubSize = input.nextInt();
			this.clubSizes[i] = clubSize;
			int[] teamsInClub = new int[clubSize];
			this.clubCapacities[i] = input.nextInt();
			for(int j = 0; j < clubSize; j++) {
				teamsInClub[j] = (input.nextInt() - 1);
			}
			this.clubs.add(teamsInClub);
		}
		
		// Again the numbering of teams is corrected
		for(int i = 0; i < this.numLeagues; i++) {
			int leagueNumber = input.nextInt();				// This number in the data is purely for a better overview of the data
			for(int j = 0; j < this.leagueSize; j++) {
				this.leagues[i][j] = (input.nextInt() - 1);
			}
		}
		
		for(int h = 0; h < this.leagueSize; h++) {
			for(int r = 0; r < this.numRounds; r++) {
				this.U[h][r] = input.nextInt();
			}
		}
		
		input.close();
	}
	
	/**
	 * This method solves the instance given from the data file using the MILP as defined by Davari et al. (2020)
	 **/
	public void solveMILPforMLSP() {
		
		double startTime = System.currentTimeMillis();
		try {
			
			IloCplex cplex = new IloCplex();
			cplex.setOut(null);
			
			// Create the discrete variable that will represent the number of violation for each club in each round
			IloNumVar[][] zVar = new IloNumVar[this.numClubs][this.numRounds];
			for(int c = 0; c < this.numClubs; c++) {
				for(int r = 0; r < this.numRounds; r++) {
					zVar[c][r] = cplex.numVar(0, Integer.MAX_VALUE, IloNumVarType.Int);			// Automatically applies the zero lower bound on z (4th constraint)
				}
			}
			
			// Create the binary variable that assigns teams to a certain HAP
			IloNumVar[][] xVar = new IloNumVar[this.numTeams][this.leagueSize];
			for(int t = 0; t < this.numTeams; t++) {
				for(int h = 0; h < this.leagueSize; h++) {
					xVar[t][h] = cplex.numVar(0, 1, IloNumVarType.Int);							//  Automatically ensures that this variable is binary
				}
			}
			
			// Adding the objective, which is the total number of violations over all clubs and rounds
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for(int c = 0; c < this.numClubs; c++) {
				for(int r = 0; r < this.numRounds; r++) {
					objective.addTerm(zVar[c][r], 1);
				}
			}
			cplex.addMinimize(objective);
			
			// Adding first constraint which states that in a league l, only one team can be assigned to a certain HAP h.
			for(int l = 0; l < this.numLeagues; l++) {
				for(int h = 0; h < this.leagueSize; h++) {
					IloLinearNumExpr constraint1 = cplex.linearNumExpr();
					for(int i = 0; i < this.leagueSize; i++) {
						int t = this.leagues[l][i];
						constraint1.addTerm(xVar[t][h], 1);
					}
					cplex.addEq(constraint1, 1);
				}
			}
			
			// Adding second constraint which states that each team t can be assigned to only one HAP h
			for(int l = 0; l < this.numLeagues; l++) {
				for(int i = 0; i < this.leagueSize; i++) {
					int t = this.leagues[l][i];
					IloLinearNumExpr constraint2 = cplex.linearNumExpr();
					for(int h = 0; h < this.leagueSize; h++) {
						constraint2.addTerm(xVar[t][h], 1);
					}
					cplex.addEq(constraint2, 1);
				}
			}
				
			// Adding third constraint which states that the discrete variable z (for each club c in each round r) should be greater or equal to 
			// the difference between the number of games scheduled to take place at club c and its capacity. Note that the fourth constraint which states that
			// z (for each club c in each round r) is non-negative is automatically applied by the definition of the variable earlier.
			// Observe that the inequality is rewritten such that all decision variables are on the LHS of the inequality.
			for(int c = 0; c < this.numClubs; c++) {
				for(int r = 0; r < this.numRounds; r++) {
					IloLinearNumExpr constraint3 = cplex.linearNumExpr();
					constraint3.addTerm(zVar[c][r], 1);
					int[] club = this.clubs.get(c);
					for(int i = 0; i < club.length; i++) {
						int t = club[i];
						for(int h = 0; h < this.leagueSize; h++) {
							constraint3.addTerm(xVar[t][h], -this.U[h][r]);			// Negative as it is brought to the LHS
						}
					}
					cplex.addGe(constraint3, -this.clubCapacities[c]);
				}
			}
			
			// Solve MILP and save values 
			cplex.solve();
			
			this.numViolations = cplex.getObjValue();
			
			for(int c = 0; c < this.numClubs; c++) {
				for(int r = 0; r < this.numRounds; r++) {
					this.z[c][r] = cplex.getValue(zVar[c][r]);
				}
			}
			
			for(int t = 0; t < this.numTeams; t++) {
				for(int h = 0; h < this.leagueSize; h++) {
					this.x[t][h] = cplex.getValue(xVar[t][h]);
				}
			}
			
			cplex.close();
		}
		
		catch (IloException exc) {
			exc.printStackTrace();
		}	
		double endTime = System.currentTimeMillis();
		
		this.runningTime = endTime - startTime;
	}
	
	/**
	 * This method outputs the solution found to the file inputed by the user.
	 **/
	public void outputSolution(String filename) throws IOException {
		
		String newWD = "../";
		File file = new File(newWD, filename);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		
		bw.write("The total number of violations according to the allocation below is equal to: " + this.numViolations);
		bw.newLine();
		bw.newLine();
		bw.write("The total running time is: " + this.runningTime + " milliseconds.");
		bw.newLine();
		bw.newLine();
		
		bw.write("The amount of violations per club in each round is: (clubs in rows, rounds in columns)");
		bw.newLine();
		for(int c = 0; c < this.numClubs; c++) {
			for(int r = 0; r < this.numRounds; r++) {
				bw.write(this.z[c][r] + "\t");
			}
			bw.newLine();
		}
		
		bw.newLine();
		bw.write("The allocation of teams to HAPs is as follows: (teams in rows, HAPs in columns)");
		bw.newLine();
		for(int t = 0; t < this.numTeams; t++) {
			for(int h = 0; h < this.leagueSize; h++) {
				bw.write(this.x[t][h] + "\t");
			}
			bw.newLine();
		}
		
		bw.close();
	}
	
}
