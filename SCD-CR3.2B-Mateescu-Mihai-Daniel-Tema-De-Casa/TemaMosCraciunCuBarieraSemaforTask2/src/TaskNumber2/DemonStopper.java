package TaskNumber2;

import java.util.concurrent.Semaphore;

public class DemonStopper extends Thread{

	static Semaphore stopDemonSemaphore = new Semaphore(0);

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
