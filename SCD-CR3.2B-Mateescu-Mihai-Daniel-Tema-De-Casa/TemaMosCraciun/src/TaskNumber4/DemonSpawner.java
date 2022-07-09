package TaskNumber4;

import java.util.Random;
/**
 * adds demons to their coven
 */
public class DemonSpawner extends Thread {
	/**
	 * minimum sleeping time
	 */
	final int MAXIMUM_SLEEPING_TIME = 1000;
	/**
	 * maximum sleeping time
	 */
	final int MINIMUM_SLEEPING_TIME = 500;
	/**
	 * array of existing covens where demons may be spawned
	 */
	public Coven[] coven;
	/**
	 * current number of covens
	 */
	int noOfCovens;
	/**
	 * Constructor. Initializes fields
	 */
	DemonSpawner(Thread[] coven, int noOfCovens){
		this.coven = (Coven[]) coven;
		this.noOfCovens = noOfCovens;
	}
	/**
	 * Method called when thread starts.
	 * The threads sleeps for a random ammount of time, then it picks a random coven and calls a method in the coven to
	 * spawn demons there. The thread stops this routine when the goal is achieved and prints the final position of demons.
	 */
	public void run() {
		Random rand = new Random();
		int randomCoven;
		int sleepingTime;
		while(Demon.demonCurrentNumberOfPotions < Application.NO_OF_POTIONS_TO_ACHIEVE) {
			sleepingTime = rand.nextInt(MAXIMUM_SLEEPING_TIME - MINIMUM_SLEEPING_TIME) + MINIMUM_SLEEPING_TIME;
			try {
				Thread.sleep(sleepingTime);
			} catch (InterruptedException e) {
				System.out.println("Demon spawner can't sleep");
				e.printStackTrace();
			}
			randomCoven = rand.nextInt(noOfCovens);
			coven[randomCoven].addDemonToCoven();
		}
		System.out.println("Demon spawner finished");
	}
}
