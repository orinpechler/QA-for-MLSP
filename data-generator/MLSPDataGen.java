import java.util.*;
import java.io.*;

/* This program allows someone to generate random (realistic) data for the multi-league scheduling problem
* The instance outputted depends on the input of an individual, namely it depends on: the number of leagues, the league size, and
* the number of clubs (which should be greater than the league size). It is assumed that:
* - the league plays according to a Double Robin Round Tournament, matches are grouped into rounds, every team can play once per round,
*   and matches of a league are scheduled using minimum number required consecutive rounds. Due to this the number of rounds immediately follows
*   from the league size. Moreover, the rounds are assumed to start from round 1 immediately. 
* - Every league has the same even number of teams. So, the number of teams follow directly from the league size and number of leagues. 
* - A club cannot consist of two teams that are part of the same league. So, every team that is part of a club, plays in a different league.
* - The HAPsets used are complimentary and feasible HAPsets, which are given for each even league size (from 4 up to 16).
* The data generated is outputed to the file in the format specified as in README.md (the file is chosen by the user)
*/
public class MLSPDataGen{

    public static void main(String args[]) {

        // This enables the user to input his/her desired values for: number of leagues, league size, number of clubs, and the file to output the data
        Scanner input = new Scanner(System.in);

        System.out.println("How many teams should every league contain?");
        int leagueSize = input.nextInt();

        System.out.println("How many leagues should there be?");
        int numLeagues = input.nextInt();

        System.out.println("How many clubs should there be? (More than number of teams per league)");
        int numClubs = input.nextInt();

        System.out.println("What is the version of this file? (A,B,C,... etc)");
        String filename = input.next();
        String fileOUT = leagueSize + "-" + numLeagues + "-" + numClubs + "-" + filename + ".txt";      // format for the file name

        // This changes the working directory of the file such that all instances are written to the folder: data
        String newWD = "../data";
        File file = new File(newWD, fileOUT);

        input.close();
        System.out.println();

        // Calculate number of teams and number of rounds according to the specified values
        int numTeams = leagueSize*numLeagues;
        int numRounds = 2*(leagueSize - 1);

        // Create random leagues over which all the teams are divided
        int[][] leagues = generateLeagues(leagueSize, numLeagues, numTeams);

        // Using the allocation of leagues, the clubs are generated
        ArrayList<Club> clubs = generateClubs(leagues, numLeagues, leagueSize, numTeams, numClubs);

        // Create parameter U that represents the HAPset corresponding to the league size.
        int[][] U = new int[leagueSize][numRounds];
        // The HAPsets are saved in text files
        try {
            U = createParameterU(leagueSize, numRounds);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        // Output data to file
        try {
            outputDataToFile(file, leagues, clubs, numClubs, numLeagues, numTeams, leagueSize, U, numRounds);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
			e.printStackTrace();
		}

    }

    /**
     * This method generates a random assignment of teams to leagues, until all leagues are full.
     * Teams are numbered from 1 to (number of teams).
     **/
    public static int[][] generateLeagues(int leagueSize, int numLeagues, int numTeams) {

        int[][] leagues = new int[numLeagues][leagueSize];

        Random rd = new Random();

        for(int j = 1; j <= numTeams; j++) {
            boolean assigned = false;
            while(!assigned) {
                int rdLeague = rd.nextInt(numLeagues);
                for(int k = 0; k < leagueSize; k++) {
                    // Only assign a team to this league, if the league is not full already!
                    if(leagues[rdLeague][k] == 0) {
                        leagues[rdLeague][k] = j;
                        assigned = true;
                        break;
                    }
                }
            }
        }

        return leagues;
    }

    /**
     * This method generates an assignment of teams to clubs, where no club can contain two teams that are in the same league. It 
     * also generates a club capacity for each club (randomly).
     **/
    public static ArrayList<Club> generateClubs(int[][] leagues, int numLeagues, int leagueSize, int numTeams, int numClubs) {

        // The clubs will be of different size, so object "Club" is used for convenience.
        // First initate all (empty) clubs
        Random rd = new Random();
        ArrayList<Club> clubs = new ArrayList<Club>();
        for(int i = 0; i < numClubs; i++) {
            ArrayList<Integer> teams = new ArrayList<Integer>();
            Club newClub = new Club(0, 0, teams);
            clubs.add(newClub);
        }

        // Now the teams are assigned to clubs, which is done by iterating through teams in a league and ensuring that a team can 
        // only be assigned to a club which does not contain any other team in the same league. (This is why also the number of clubs has to 
        // be greater than the league size)
        for(int i = 0; i < numLeagues; i++) {
            int[] usedClubs = new int[numClubs];
            for(int j = 0; j < leagueSize; j++) {
                boolean flag = false;
                while(!flag) {
                    int rdClubIndex = rd.nextInt(numClubs);
                    if(usedClubs[rdClubIndex] == 0) {
                        // Add team to the club object
                        Club rdClub = clubs.get(rdClubIndex);
                        rdClub.addNewTeam(leagues[i][j]);
                        clubs.remove(rdClubIndex);
                        clubs.add(rdClubIndex, rdClub);
                        usedClubs[rdClubIndex] = 1;
                        flag = true;
                    }
                    else{
                        continue;
                    }
                }
            }
        }

        // According to the assinment of teams to clubs, the club capacity is randomized
        // In particular, the club capacity is randomly chosen in the range that is also used
        // in Li et al. (2022) "Multi-league sports scheduling with different league sizes"
        for(int i = 0; i < clubs.size(); i++) {
            int size = clubs.get(i).getClubsize();
            if(size == 0) {
                System.out.println("Error, there is an empty club, try again or reduce number of clubs.");
            }
            int upper = Math.min(((int) Math.floor(size/2) + 2), size);
            int lower = Math.max(((int) Math.floor(size/2) - 2), 1);
            int randomCap = rd.nextInt(upper - lower + 1) + lower;
            clubs.get(i).setCapacity(randomCap);
        }

        return clubs;
    }

    /**
     * This method creates the parameter U as specified in the MLSP. That is, for each HAPset h, for each round, it
     * contains value 1 if the team assigned to this HAP plays at home, and zero otherwise.
     */
    public static int[][] createParameterU(int leagueSize, int numRounds) throws FileNotFoundException {

        int[][] U = new int[leagueSize][numRounds];

        String filename = null;
        if(leagueSize == 4) {
            filename = "HAPset_for_4.txt";
        } else if(leagueSize == 6) {
            filename = "HAPset_for_6.txt";
        } else if(leagueSize == 8) {
            filename = "HAPset_for_8.txt";
        } else if(leagueSize == 10) {
            filename = "HAPset_for_10.txt";
        } else if(leagueSize == 12) {
            filename = "HAPset_for_12.txt";
        } else if(leagueSize == 14) {
            filename = "HAPset_for_14.txt";
        } else if(leagueSize == 16) {
            filename = "HAPset_for_16.txt";
        } else {
            System.out.println("Error, too high league size");
        }
        
        File file = new File(filename);
        Scanner input = new Scanner(file);

        for(int i = 0; i < leagueSize; i++) {
            for(int j = 0; j < numRounds; j++) {
                String letter = input.next();
                if(letter.equals("H")) {
                    U[i][j] = 1;
                } else if(letter.equals("A")) {
                    U[i][j] = 0;
                } 
            }
        }

        input.close();

        return U;
    }

    /**
     * This method output the generated data to the filename inputted by the user
     */
    public static void outputDataToFile(File file, int[][] leagues, ArrayList<Club> clubs, int numClubs, int numLeagues, int numTeams, int leagueSize, int[][] U, int numRounds) throws IOException {

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        bw.write(numTeams + "\t" + numLeagues + "\t" + numClubs + "\t" + leagueSize);
        bw.newLine();
        bw.newLine();

        for(int i = 0; i < clubs.size(); i++) {
            Club temp = clubs.get(i);
            bw.write(temp.getClubsize() + "\t" + temp.getCapacity() + '\t');
            for(int j = 0; j < temp.getClubsize(); j++) {
                bw.write(temp.getTeams().get(j) + "\t");
            }
            bw.newLine();
        }

        bw.newLine();
        for(int i = 0; i < numLeagues; i++) {
            bw.write((i+1) + "\t");
            for(int j = 0; j < leagueSize; j++) {
                bw.write(leagues[i][j] + "\t");
            }
            bw.newLine();
        }
        bw.newLine();

        for(int i = 0; i < leagueSize; i++) {
            for(int j = 0; j < numRounds; j++) {
                bw.write(U[i][j] + "\t");
            }
            bw.newLine();
        }

        bw.close();
    }
}