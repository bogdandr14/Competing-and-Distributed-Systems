package facility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import utility.Generator;

/**
 * This class is used to monitor the production robots actions, such as adding a
 * new robot or moving a robot.
 * 
 * @author Bogdan Draghici
 *
 */
public class RobotsMonitor {
	private int facilitySize, facilityId;

	private volatile List<ProductionRobot> productionRobots = new ArrayList<ProductionRobot>();
	private volatile ReentrantLock movementLock;

	/**
	 * Public constructor for the facility robots monitor.
	 * 
	 * @param facilityId   The ID of this facility.
	 * @param facilitySize The size of this facility
	 * @param movementLock The lock used when trying to move a production robot
	 */
	public RobotsMonitor(int facilityId, int facilitySize, ReentrantLock movementLock) {
		this.facilitySize = facilitySize;
		this.facilityId = facilityId;
		this.movementLock = movementLock;
	}

	/**
	 * Try to change the location of a production robot in the facility. This is a
	 * synchronized method. Only one robot can move at a time, in order to avoid the
	 * situation in which 2 robots move to the same location simultaneously.
	 * 
	 * @param newLocation     The location where the robot tries to move.
	 * @param productionRobot The production robot that tries to move.
	 * @return <code>true</code> in case the robot moved successfully of
	 *         <code>false</code> contrary.
	 */
	public boolean moveProductionRobot(int[] newLocation, ProductionRobot productionRobot) {
		if (newLocation[0] < 0 || newLocation[0] >= facilitySize || newLocation[1] < 0
				|| newLocation[1] >= facilitySize) {
			return false;
		}
		boolean isAvailable = true;
		synchronized (productionRobots) {
			movementLock.lock();
			for (int i = 0; i < productionRobots.size(); ++i) {
				ProductionRobot pr = productionRobots.get(i);
				if (newLocation == pr.getLocation()) {
					isAvailable = false;
				}
			}
			if (isAvailable) {
				productionRobot.setLocation(newLocation);
			}
			movementLock.unlock();
		}
		return isAvailable;
	}

	/**
	 * Adds a new robot to the list of existent production robots in the facility.
	 * 
	 * @param productionRobot The production robot to be added.
	 */
	public void addNewRobot(ProductionRobot productionRobot) {
		int[] location = new int[2];
		do {
			location[0] = Generator.createNumber(0, facilitySize - 1);
			location[1] = Generator.createNumber(0, facilitySize - 1);
		} while (!moveProductionRobot(location, productionRobot));
		synchronized (productionRobots) {
			productionRobots.add(productionRobot);
			int robotId = facilityId * 1000 + productionRobots.size();
			productionRobot.setRobotId(robotId);
//			System.out.println("New robot added: " + robotId);
		}
		productionRobot.start();
	}

	/**
	 * Retrieves the locations of all the production robots that work in this
	 * facility.
	 * 
	 * @return A list with the locations of the production robots
	 */
	public List<int[]> getRobotsLocations() {
		List<int[]> robotsLocations = new ArrayList<>();
		productionRobots.forEach((ProductionRobot pr) -> {
			robotsLocations.add(pr.getLocation());
		});
		return robotsLocations;
	}

	/**
	 * Returns the number of production robots that work in the facility.
	 * 
	 * @return The number of the production robots
	 */
	public int getNoRobots() {
		return productionRobots.size();
	}
}
