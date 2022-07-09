package TaskNumber2;

import java.util.Random;
public class DemonSpawner extends Thread {

	final int MAXIMUM_SLEEPING_TIME = 1000;
	final int MINIMUM_SLEEPING_TIME = 500;
	public Coven[] coven;
	int noOfCovens;

	DemonSpawner(Thread[] coven, int noOfCovens){
		this.coven = (Coven[]) coven;
		this.noOfCovens = noOfCovens;
	}
	
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
