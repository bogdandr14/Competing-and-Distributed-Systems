package headquarter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import facility.ProductionFacility;
import utility.Generator;

/**
 * The robot with the task of packing the vaccine doses received from the
 * production facilities and delivering them to the headquarters.
 * 
 * @author Bogdan Draghici
 *
 */
public class PackingRobot extends Thread {
	private static final int SERVER_PORT = 6789;
	private static final int MIN_READ_WAIT_TIME = 5;
	private static final int MAX_READ_WAIT_TIME = 10;

	private volatile static boolean isWorking = true;

	private List<ProductionFacility> productionFacilities;

	/**
	 * Public constructor for a packing robot.
	 * 
	 * @param productionFacilities A list of VaccinePools from which to take the
	 *                             created vaccines.
	 */
	public PackingRobot(List<ProductionFacility> productionFacilities) {
		this.productionFacilities = productionFacilities;
	}

	/**
	 * The runnable method the packing robot thread. The packing robot will randomly
	 * choose a production facility from which to read the number of available
	 * vaccine doses. In case there are any doses waiting for transportation, the
	 * packing robot will try to retrieve one of them and send it via TCP to the
	 * headquarters. After getting the confirmation that the vaccine dose was
	 * received, the packing robot will wait for a period of time before trying to
	 * transport another vaccine.
	 */
	public void run() {
		while (isWorking) {
			int facilityNumber = Generator.createNumber(0, productionFacilities.size() - 1);
			ProductionFacility productionFacility = productionFacilities.get(facilityNumber);
			int noVaccines, vaccineId;
			do {
				noVaccines = productionFacility.getNoVaccines();
			} while (noVaccines == -1);
			if (noVaccines > 0) {
				do {
					vaccineId = productionFacility.packVaccine();
				} while (vaccineId == -1);
				transportVaccine(vaccineId);
			}
			try {
				sleep(Generator.createNumber(MIN_READ_WAIT_TIME, MAX_READ_WAIT_TIME));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * The transportation method that is used to deliver a vaccine to the
	 * headquarters. Due to the fact only one vaccine can be processed at a time by
	 * the headquarters, the transportation channel is unique and shared by all
	 * packing robots. The packing robot that transport the vaccine dose will wait
	 * for the confirmation of receiving it.
	 * 
	 * @param vaccine The vaccine to transport.
	 */
	private static synchronized void transportVaccine(int vaccine) {
		Socket s = null;
		int data = -1;
		try {
			s = new Socket("localhost", SERVER_PORT);
			while (data != vaccine) {
				DataInputStream in = new DataInputStream(s.getInputStream());
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
				out.writeInt(vaccine);
				sleep(0, 1); //Waits for a nanosecond before reading from the input stream. 
				data = in.readInt(); //Without this delay there might appear a null pointer exception.
			}
		} catch (UnknownHostException e) {
			System.out.println("Socket:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("readline:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (IOException e) {
					System.out.println("close:" + e.getMessage());
				}
		}
	}

	/**
	 * Stops the working process for the packing robot.
	 */
	public static void stopWorking() {
		isWorking = false;
	}
}
