package TaskNumber2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Witch extends Thread {
	
	int numberOfCovens;
	public IPotionQueue potionQueue[];
	int id;
	int[] witchesPerCoven;
	int maxNumberOfWitchesThatCanRead = 10;
	static ReentrantLock witchLock = new ReentrantLock();

	Witch(int id, IPotionQueue potionQueue[], int numberOfCovens){
		this.id = id;
		this.potionQueue = potionQueue;
		this.numberOfCovens = numberOfCovens;
		witchesPerCoven = new int[numberOfCovens];
		for(int i = 0; i < numberOfCovens; i++) {
			witchesPerCoven[i] = 0;
		}
	}
	
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
