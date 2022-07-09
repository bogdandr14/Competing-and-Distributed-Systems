package headquarter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import facility.ProductionFacility;
import facility.ProductionRobot;
import utility.Generator;

/**
 * The headquarters of the company that contains everything in the company,
 * creates the production robots and distributes them in the production
 * facilities, receives the vaccines created and stops all the processes when
 * all the vaccine doses have been received.
 * 
 * @author Bogdan Draghici
 *
 */
public class PharmaceuticalCompany extends Thread {
	private static final int MIN_WAIT_ROBOT_CREATION = 500;
	private static final int MAX_WAIT_ROBOT_CREATION = 1000;

	private List<ProductionFacility> productionFacilities;
	private List<PackingRobot> packingRobots;
	private VaccineReceiver vaccineReceiver;

	/**
	 * Public constructor for the pharmaceutical company. In this constructor are
	 * generated the production facilities, the packing robots and the number of
	 * vaccines that must be received.
	 */
	public PharmaceuticalCompany() {
		productionFacilities = Generator.generateProductionFacilities();
		packingRobots = Generator.generatePackingRobots(productionFacilities);
		vaccineReceiver = new VaccineReceiver(Generator.generateNoVaccines());
	}

	/**
	 * The runnable method for the thread. After a given period of time generates a
	 * new production robot which is assigned randomly in a non-full production
	 * facility. After the requested number of vaccine doses is created, the company
	 * will wait till all of them are transported to the headquarters. In the end
	 * the company will stop the working process of the packing robots and vaccine
	 * receiver (TCP server) and notify the total number of available vaccine doses.
	 */
	public void run() {
		long startTime = Calendar.getInstance().getTimeInMillis();
		startWorking();
		while (tryCreateAndAddProductionRobot() && productionFacilities.get(0).isAlive()) {
			try {
				sleep(Generator.createNumber(MIN_WAIT_ROBOT_CREATION, MAX_WAIT_ROBOT_CREATION));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		boolean receivingVaccines = true;
		int vaccinesReceived = vaccineReceiver.getVaccinesCreated();
		while (receivingVaccines) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (vaccinesReceived == vaccineReceiver.getVaccinesCreated()) {
				receivingVaccines = false;
			} else {
				vaccinesReceived = vaccineReceiver.getVaccinesCreated();
			}
		}
		stopWorking();
		long endTime = Calendar.getInstance().getTimeInMillis();
		System.out.println("~~~~~The company finished receiving vaccines after " + ((double) (endTime - startTime) / 1000)
				+ " seconds and has " + vaccinesReceived + " vaccine doses available.~~~~~");
	}

	/**
	 * Starts the working process in the production facilities, the server for
	 * receiving the vaccines and the packing robots that delivers those vaccines.
	 */
	private synchronized void startWorking() {
		productionFacilities.forEach((ProductionFacility pf) -> {
			pf.start();
		});
		vaccineReceiver.start();
		packingRobots.forEach((PackingRobot pr) -> {
			pr.start();
		});
	}

	/**
	 * Stops the whole process when all the created vaccine doses were received by
	 * the headquarters.
	 */
	private void stopWorking() {
		VaccineReceiver.stopWorking();
		PackingRobot.stopWorking();
	}

	/**
	 * Checks if there exists at least one production facility which can add a new
	 * production robot to its workforce, and then creates a new production robot
	 * and assigns it to a production facility that has not reached the maximum
	 * number of production robots allowed in the facility.
	 * 
	 * @return <code>true</code> if a new production robot was created and added to
	 *         a production facility or <code>false</code> if all the production
	 *         facilities are full and there can not be added another production
	 *         robot
	 */
	private boolean tryCreateAndAddProductionRobot() {
		List<ProductionFacility> nonFullProductionFacilities = new ArrayList<>();
		productionFacilities.forEach((ProductionFacility productionFacility) -> {
			if (productionFacility.canReceiveRobot()) {
				nonFullProductionFacilities.add(productionFacility);
			}
		});
		if (nonFullProductionFacilities.size() == 0) {
			return false;
		}
		int facility = Generator.createNumber(0, nonFullProductionFacilities.size() - 1);
		ProductionFacility pf = nonFullProductionFacilities.get(facility);
		pf.getFacilityRobots().addNewRobot(new ProductionRobot(pf));
		return true;
	}

	public static void main(String[] args) {
		PharmaceuticalCompany pc = new PharmaceuticalCompany();
		pc.start();
	}
}
