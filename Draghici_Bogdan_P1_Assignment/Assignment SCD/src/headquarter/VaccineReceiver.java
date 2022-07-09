package headquarter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import facility.ProductionFacility;

/**
 * The receiver that manages all the vaccine doses that come in the headquarters
 * via TCP.
 * 
 * @author Bogdan Draghici
 *
 */
public class VaccineReceiver extends Thread {
	private static final int SERVER_PORT = 6789;
	private static ServerSocket listenSocket;
	private volatile static boolean isWorking = true;

	private int noVaccinesToReceive;

	private volatile List<Integer> vaccinesReceived = new ArrayList<Integer>();

	/**
	 * Public constructor for the vaccine receiver.
	 * 
	 * @param noVaccinesToReceive The numbers of vaccines to receive.
	 */
	public VaccineReceiver(int noVaccinesToReceive) {
		this.noVaccinesToReceive = noVaccinesToReceive;
	}

	/**
	 * The runnable method for the VaccineReceiver thread. Has the role of a TCP
	 * server in order to receive the vaccine doses from clients(packing robots).
	 */
	@Override
	public void run() {
		isWorking = true;
		try {
			listenSocket = new ServerSocket(SERVER_PORT);
			while (isWorking) {
				Socket clientSocket = listenSocket.accept();
				DataInputStream in = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				int vaccine = in.readInt();
				out.writeInt(vaccine);
				addNewVaccine(vaccine);
				clientSocket.close();
			}
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("readline:" + e.getMessage());
		} finally {
			try {
				listenSocket.close();
			} catch (IOException e) {
				System.out.println("Listen socket:" + e.getMessage());
			}
		}
	}

	/**
	 * Receive the vaccine from the packing robots and add it to the list of
	 * vaccines in the headquarter.
	 * 
	 * @param vaccine The new vaccine ID.
	 */
	private void addNewVaccine(Integer vaccine) {
		synchronized (vaccinesReceived) {
			vaccinesReceived.add(vaccine);
			if (vaccinesReceived.size() % 1000 == 0) {
				System.out.println("---Vaccines received so far: " + vaccinesReceived.size());
			}
			if (vaccinesReceived.size() == noVaccinesToReceive) {
				ProductionFacility.stopWorking();
				System.out.println("~~~~~The requested number of vaccine doses has been reached~~~~~");
			}
		}
	}

	/**
	 * Gives the number of vaccine doses that have arrived to the headquarters.
	 * 
	 * @return The number of vaccines received.
	 */
	protected int getVaccinesCreated() {
		return vaccinesReceived.size();
	}

	/**
	 * Stops the working process for the vaccine receiver thread(TCP server).
	 */
	protected static void stopWorking() {
		isWorking = false;
	}
}
