import java.util.*;

// Orin Pechler, i6297093 Maastricht University

/*
 * This program creates a "Club" object which stores the club's size, capacity and teams that belong to it. 
 * An object is used as teams will be assigned to clubs randomly and the size of a club follows from that assignment.
 * Hence, the sizes are not known prior to the process of generating the data, and this object is used to conveniently use it.
 */
public class Club {
    
    private int clubsize;
    private int capacity;
    private ArrayList<Integer> teams;               

    /**
     * Creates an object that stores the club's size, capacity and teams. The teams that 
     * belong to the club are stored as an arraylist of integers.
     */
    public Club(int clubsize, int capacity, ArrayList<Integer> teams) {

        this.clubsize = clubsize;
        this.capacity = capacity;
        this.teams = teams; 
    }

    /**
     * Returns the number of teams contained in the club
     */
    public int getClubsize() {
        return this.clubsize;
    }

    /**
     * Returns the club's capacity
     */
    public int getCapacity() {
        return this.capacity;
    }

    /**
     * Returns an array list of integers that contains the teams which are part of the club
     */
    public ArrayList<Integer> getTeams() {
        return this.teams;
    }

    /**
     * This method allows the club's capacity to be set to a certain value
     */
    public void setCapacity(int newCapacity) {
        this.capacity = newCapacity;
    }

    /**
     * This method adds a certain team to the club and updates the club's size accordingly
     */
    public void addNewTeam(int team) {
        this.teams.add(team);
        this.clubsize++;
    }
}
