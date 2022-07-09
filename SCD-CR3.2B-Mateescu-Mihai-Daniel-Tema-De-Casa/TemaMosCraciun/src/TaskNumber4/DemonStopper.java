package TaskNumber4;

import java.util.concurrent.Semaphore;
/**
 * releases a semaphore once in a while. Demons try to get the semaphore in order to retire
 */
public class DemonStopper extends Thread{
	/**
	 * semaphore initially blocked. used for demon retirement
	 */
	static Semaphore stopDemonSemaphore = new Semaphore(0);
	
	/**
	 * Method called when thread starts.
	 * The threads sleeps for a random amount of time,it releases a permit to the semaphore. The demons will try to get it in order
	 * to retire
	 */
	public void run() {
		while(Demon.demonCurrentNumberOfPotions < Application.NO_OF_POTIONS_TO_ACHIEVE) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.println("Demon stopper can't sleep");
				e.printStackTrace();
			}
			stopDemonSemaphore.release();
		}
		System.out.println("Demon stopper finished");
	}
}
