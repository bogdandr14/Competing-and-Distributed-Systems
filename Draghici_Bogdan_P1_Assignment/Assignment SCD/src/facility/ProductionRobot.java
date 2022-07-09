package facility;

import java.util.ArrayList;
import java.util.List;

import utility.Generator;

/**
 * The production robot that is creating the vaccines in the production
 * facility.
 * 
 * @author Bogdan Draghici
 *
 */
public class ProductionRobot extends Thread {
	private static final int MIN_MOVE_WAIT_TIME = 10;
	private static final int MAX_MOVE_WAIT_TIME = 50;
	private static final int VACCINE_CREATED_WAIT_TIME = 30;
	private static final int[][] moves = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 } };
	
	private volatile static boolean isWorking = true;

	private int noVaccines, robotId;
	private volatile int[] location;
	private ProductionFacility facility;

	/**
	 * Public constructor for the production robot.
	 * 
	 * @param facility The facility in which the robot is assigned.
	 */
	public ProductionRobot(ProductionFacility facility) {
		this.facility = facility;
		noVaccines = 0;
	}

	/**
	 * The runnable method of the production robot thread. While the robot is
	 * working, it will try to move in one direction in order to create a new
	 * vaccine, waiting a random period of time in a specific interval in either
	 * case it succeeds to create a vaccine or not.
	 */
	public void run() {
		while (isWorking) {
			int sleepTime = Generator.createNumber(MIN_MOVE_WAIT_TIME, MAX_MOVE_WAIT_TIME);
			if (tryMove()) {
				facility.notifyNewVaccine(robotId * 10000 + (++noVaccines));
				sleepTime = 4 * VACCINE_CREATED_WAIT_TIME;
			}
			try {
				sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Try to move the robot one position left, right, up or down in order to create
	 * a new vaccine.
	 * 
	 * @return <code> true</code> if the robot was able to move in one of the
	 *         specified directions, or <code>false</code> contrary.
	 */
	private boolean tryMove() {
		List<Integer> movesTried = new ArrayList<>();
		int move;
		boolean robotMoved = false;
		while (movesTried.size() < 4 && !robotMoved) {
			do {
				move = (int) (Math.random() * 4);
			} while (movesTried.contains(move));
			movesTried.add(move);
			robotMoved = facility.getFacilityRobots().moveProductionRobot(
					new int[] { location[0] + moves[move][0], location[1] + moves[move][1] }, this);
		}
		return robotMoved;
	}

	/**
	 * Get the location of the production robot.
	 * 
	 * @return The location of the robot.
	 */
	public int[] getLocation() {
		return location;
	}

	/**
	 * Set the location of the newly added production robot.
	 * 
	 * @param newLocation The new location.
	 */
	public void setLocation(int[] newLocation) {
		location = newLocation;
	}

	/**
	 * Set the ID of the newly added production robot.
	 * 
	 * @param id The ID of the robot.
	 */
	public void setRobotId(int id) {
		robotId = id;
	}

	/**
	 * Stops the working process of the production robot.
	 */
	public static void stopWorking() {
		isWorking = false;
	}
}
