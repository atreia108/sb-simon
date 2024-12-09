/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- Project Simon ---------------------------------------------------------------------*
 * -- Start Date: 23rd November, 2022 ---------------------------------------------------*
 * -- Last Update: 1st December, 2022 ---------------------------------------------------*
 * -- Hridyanshu Aatreya <2200096@brunel.ac.uk> -----------------------------------------*
 * -- Yellow 46 -------------------------------------------------------------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *
 * -- The Step class represents each individual step object in an ArrayList of steps    -*
 * -- which is played to the user each round. They must attempt to repeat each Step in  -*
 * -- the ArrayList or the round is lost. This class is a core element of the Classic   -*
 * -- Engine and is extensively used for comparison and validation. ---------------------*
 * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

public class Step {
	private String color;
	private int points;
	
	Step(String x, int y) {
		color = x;
		points = y;
	}
	
	public String getColor() {
		return color;
	}
	
	public int getPoints() {
		return points;
	}
	
	public void Print() {
		System.out.print(("(" + getColor()));
		System.out.print(", ");
		System.out.print(getPoints());
		System.out.print(") ");
	}
}
