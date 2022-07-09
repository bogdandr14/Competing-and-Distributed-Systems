package TaskNumber4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Witch extends Thread {
	/**
	 * total number of covens (random between 2 and 5)
	 */
	int numberOfCovens;
	/**
	 * potion queue, synchronized with monitors, common with demons and covens
	 */
	public IPotionQueue potionQueue[];
	/**
	 * witch identifier
	 */
	int id;
	/**
	 * array that keeps track of the number of witches that read every coven (can't be greater than 10 at a time)
	 */
	int[] witchesPerCoven;
	/**
	 * maximum  number of witches that can read a coven at a time
	 */
	int maxNumberOfWitchesThatCanRead = 10;
	/**
	 * lock to protect incrementation and decrementation of the number of witches per coven variables
	 */
	static ReentrantLock witchLock = new ReentrantLock();
	/**
     * Constructor of the Witch class
     * <p>It initializes private members
     * @param id witch identifier
     * @param potionQueue common for demons, witches and covens; witch read potions from here
     * @param numberOfCovens number of existing covens
     */
	Witch(int id, IPotionQueue potionQueue[], int numberOfCovens){
		this.id = id;
		this.potionQueue = potionQueue;
		this.numberOfCovens = numberOfCovens;
		witchesPerCoven = new int[numberOfCovens];
		for(int i = 0; i < numberOfCovens; i++) {
			witchesPerCoven[i] = 0;
		}
	}
	/**
     * method called when each thread is started. 
     * <p>
     * each witch is a client to the santa server. he randomly picks a free coven and reads potions from its queue
     * when demons are not writing. When they succeed they send the potions by tcp/ip to the server.
     * they stop when the potions goal is achieved.
     * </p>
     */
	public void run() {
		Socket socket = null;
		int serverPort = 6789;
		Random rand = new Random();
		while(Application.CURRENT_NO_OF_POTIONS < Application.NO_OF_POTIONS_TO_ACHIEVE) {
			int currentCoven = rand.nextInt(numberOfCovens);
			while(witchesPerCoven[currentCoven] > maxNumberOfWitchesThatCanRead && Application.CURRENT_NO_OF_POTIONS < Application.NO_OF_POTIONS_TO_ACHIEVE) {
				currentCoven = rand.nextInt(numberOfCovens);
			}
			witchLock.lock();
			witchesPerCoven[currentCoven]++;
			witchLock.unlock();
			Potion potion = potionQueue[currentCoven].pop();
			if(potion!=null){
				try {
					socket = new Socket("localhost", serverPort);
					DataInputStream in = new DataInputStream(socket.getInputStream());
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					out.writeUTF("Witch sent one potion to Santa"); // UTF is a string encoding.
					String data = in.readUTF();
					System.out.println("Received: "+ data) ;
					Application.CURRENT_NO_OF_POTIONS++;
					System.out.println(Application.CURRENT_NO_OF_POTIONS);
				}
				catch (UnknownHostException e) {
					System.out.println("Socket:"+e.getMessage());
				} catch (EOFException e) {
					System.out.println("EOF:"+e.getMessage());
				} catch (IOException e) {
					System.out.println("Readline:"+e.getMessage());
				} finally {
					if (socket != null)
						try {
							socket.close();
						} catch (IOException e) {
							System.out.println("Close:"+e.getMessage());
						}
				}
			}
			witchLock.lock();
			witchesPerCoven[currentCoven]--;
			witchLock.unlock();
		}
		System.out.println("Witch " + id + " finished");
	}
	
}
