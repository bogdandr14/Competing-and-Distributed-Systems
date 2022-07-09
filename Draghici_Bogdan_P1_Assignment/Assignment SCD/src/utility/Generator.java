package utility;

import java.util.ArrayList;
import java.util.List;

import facility.ProductionFacility;
import headquarter.PackingRobot;

/**
 * Generator for the number of production facilities, number of vaccines to
 * create, number of packing robots and integer numbers in a given interval.
 * 
 * @author Bogdan Draghici
 *
 */
public class Generator {
	private static final int MIN_NO_FACILITIES = 2;
	private static final int MAX_NO_FACILITIES = 5;

	private static final int MIN_FACILITY_SIZE = 100;
	private static final int MAX_FACILITY_SIZE = 500;

	private static final int MIN_NO_VACCINES = 20000;
	private static final int MAX_NO_VACCINES = 50000;

	private static final int MIN_NO_PACKING_ROBOTS = 8;
	private static final int MAX_NO_PACKING_ROBOTS = 50;

	/**
	 * Creates an integer number in a given interval and returns it.
	 * 
	 * @param minVal The minimum value of the number.
	 * @param maxVal The maximum value of the number.
	 * @return The integer number created.
	 */
	public static int createNumber(int minVal, int maxVal) {
		return (int) (Math.random() * (maxVal - minVal + 1) + minVal);
	}

	/**
	 * Generates the production facilities, their sizes and add them to a list.
	 * 
	 * @return The production facilities.
	 */
	public static ArrayList<ProductionFacility> generateProductionFacilities() {
		int noProductionFacilities = createNumber(MIN_NO_FACILITIES, MAX_NO_FACILITIES);
		System.out.println("The pharmaceutical company has " + noProductionFacilities + " production facilities");
		ArrayList<ProductionFacility> productionFacilities = new ArrayList<ProductionFacility>(noProductionFacilities);
		for (int i = 1; i <= noProductionFacilities; ++i) {
			int facilitySize = createNumber(MIN_FACILITY_SIZE, MAX_FACILITY_SIZE);
			productionFacilities.add(new ProductionFacility(i, facilitySize));
		}
		return productionFacilities;
	}

	/**
	 * Generates the packing robots and give them the production facilities which
	 * they must check for new vaccines.
	 * 
	 * @return The packing robots.
	 */
	public static ArrayList<PackingRobot> generatePackingRobots(List<ProductionFacility> productionFacilities) {
		int noPackingRobots = createNumber(MIN_NO_PACKING_ROBOTS, MAX_NO_PACKING_ROBOTS);
		System.out.println("The pharmaceutical company has " + noPackingRobots + " packing robots");
		ArrayList<PackingRobot> packingRobots = new ArrayList<PackingRobot>(noPackingRobots);
		for (int i = 0; i < noPackingRobots; ++i) {
			packingRobots.add(new PackingRobot(productionFacilities));
		}
		return packingRobots;
	}

	/**
	 * Generates the number of vaccines that must be created by a production
	 * facility.
	 */
	public static int generateNoVaccines() {
		int noVaccinesToReceive = createNumber(MIN_NO_VACCINES, MAX_NO_VACCINES);
		System.out.println("The pharmaceutical company must create " + noVaccinesToReceive + " vaccine doses.");
		return noVaccinesToReceive;
	}
}
