package facility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import utility.Generator;

/**
 * The production facility which contains the production robots and stores the
 * vaccines created by the production robots till they are taken by the packing
 * robots.
 * 
 * @author Bogdan Draghici
 *
 */
public class ProductionFacility extends Thread {
	private static final int MIN_LOCATION_REQUEST_TIME = 3000;
	private static final int MAX_LOCATION_REQUEST_TIME = 5000;
	private static final int NO_SEMAPHORE_PERMITS = 10;

	private static volatile boolean isWorking = true;

	private RobotsMonitor robotsMonitor;
	private int facilityId, maxNoRobots;

	private volatile int vaccinesCreated;
	private volatile Queue<Integer> vaccineIds = new LinkedList<Integer>();
	private volatile List<int[]> robotsLocations = new ArrayList<>();

	private volatile ReentrantLock requestLock = new ReentrantLock();
	private volatile ReentrantLock notificationLock = new ReentrantLock();
	private volatile Semaphore semaphore;

	/**
	 * Public constructor for the production facilities
	 * 
	 * @param facilityId   The facility id, used for the serial of the vaccine.
	 * @param facilitySize The size of the facility, displayed in the form of a
	 *                     square.
	 */
	public ProductionFacility(int facilityId, int facilitySize) {
		this.facilityId = facilityId;
		this.maxNoRobots = facilitySize / 2;
		semaphore = new Semaphore(NO_SEMAPHORE_PERMITS);
		robotsMonitor = new RobotsMonitor(facilityId, facilitySize, requestLock);
		System.out.println("Facility " + facilityId + " has size of " + facilitySize);
	}

	/**
	 * The runnable of the thread. This method will request the locations of the
	 * production robots at a random interval, while the desired number of vaccines
	 * is not met.
	 */
	@Override
	public void run() {
		long startTime = Calendar.getInstance().getTimeInMillis();
		while (isWorking) {
			try {
				sleep(Generator.createNumber(MIN_LOCATION_REQUEST_TIME, MAX_LOCATION_REQUEST_TIME));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			requestRobotsLocations();
			System.out.print(
					"Facility " + facilityId + " has " + vaccineIds.size() + " vaccines waiting for transportation and "
							+ robotsMonitor.getNoRobots() + " robots with their locations: ");
			robotsLocations.forEach((location) -> {
				System.out.print("[" + location[0] + ", " + location[1] + "], ");
			});
			System.out.println();
		}
		long endTime = Calendar.getInstance().getTimeInMillis();
		System.out.println("*****The production facility " + facilityId + " finished producing " + vaccinesCreated
				+ " vaccines in " + ((double)(endTime - startTime) / 1000) + " seconds*****");
	}

	/**
	 * Request the current locations of the production robots that work in the
	 * facility. This is a synchronized method so that there will be only one
	 * request at a time.
	 */
	private synchronized void requestRobotsLocations() {
		requestLock.lock();
		robotsLocations = robotsMonitor.getRobotsLocations();
		requestLock.unlock();
	}

	/**
	 * Notify the facility that a new dose of vaccine was created by a production
	 * robot and add it to the list of vaccines that are waiting to be transported
	 * to the headquarters. This is a synchronized method, in order to avoid the
	 * case in which 2 production robots notify about a new vaccine simultaneously
	 * and the facility tries to add them to the list of vaccines in the same time.
	 * 
	 * @param vaccineId The ID of the newly created vaccine.
	 */
	protected synchronized void notifyNewVaccine(int vaccineId) {
		notificationLock.lock();
		synchronized (vaccineIds) {
			vaccineIds.add(vaccineId);
			++vaccinesCreated;
		}
		notificationLock.unlock();
	}

	/**
	 * Informs about the number vaccines created by the production workers that
	 * still are in the production facility, waiting to be transported to the
	 * headquarters.
	 * 
	 * @return the number of vaccines in the facility or <code>-1<code> in case it
	 *         is not allowed to read the number of vaccines now.
	 */
	public int getNoVaccines() {
		int noVaccines = -1;
		try {
			semaphore.acquire();
			if (!requestLock.isLocked() && !notificationLock.isLocked()) {
				synchronized (vaccineIds) {
					noVaccines = vaccineIds.size();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		return noVaccines;
	}

	/**
	 * Retrieve a vaccine from the production facility, thus removing it from the
	 * stock of this facility. This method is used by the packing robots to get a
	 * vaccine to transport to the headquarters.
	 * 
	 * @return the oldest vaccine created in this facility or <code>-1</code> in
	 *         case it is not allowed to retrieve a vaccine now.
	 */
	public int packVaccine() {
		int vaccineId = -1;
		try {
			semaphore.acquire();
			if (!requestLock.isLocked() && !notificationLock.isLocked()) {
				synchronized (vaccineIds) {
					if (!vaccineIds.isEmpty()) {
						vaccineId = vaccineIds.poll();
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			semaphore.release();
		}
		return vaccineId;
	}

	/**
	 * The company received the requested number of vaccine doses and now ends all
	 * the working process.
	 */
	public static void stopWorking() {
		isWorking = false;
		ProductionRobot.stopWorking();
	}

	/**
	 * Return the object that contains the production robots of this facility.
	 * 
	 * @return The robots monitor.
	 */
	public RobotsMonitor getFacilityRobots() {
		return robotsMonitor;
	}

	/**
	 * Informs if a new production robot can be added to the facility or not.
	 * 
	 * @return <code>true</code> if there is space for a new robot of
	 *         <code>false</code> in case the facility is at full capacity.
	 */
	public boolean canReceiveRobot() {
		return (robotsMonitor.getNoRobots() < maxNoRobots);
	}
}
