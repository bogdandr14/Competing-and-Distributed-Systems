package TaskNumber3;

import java.util.Random;

public class Application {

	static final int MINIMUM_NO_OF_COVENS = 3;
	static final int MAXIMUM_NO_OF_COVENS = 20;
	static final int MINIMUM_COVEN_DIMENSION = 100;
	static final int MAXIMUM_COVEN_DIMENSION = 500;
	static final int MINIMUM_NO_OF_WITCHES = 8;
	static final int MAXIMUM_NO_OF_WITCHES = 12;
	static final int NO_OF_POTIONS_TO_ACHIEVE = 10000;
	static volatile int CURRENT_NO_OF_POTIONS = 0;

	public static void main(String[] args) {

		Random rand = new Random();
		int numberOfCovens = rand.nextInt(MAXIMUM_NO_OF_COVENS - MINIMUM_NO_OF_COVENS) + MINIMUM_NO_OF_COVENS;
		int numberOfWitches = rand.nextInt(MAXIMUM_NO_OF_WITCHES - MINIMUM_NO_OF_WITCHES) + MINIMUM_NO_OF_WITCHES;
		System.out.println("Number of witches: " + numberOfWitches);
		System.out.println("Number of covens: " + numberOfCovens);

		int[] covenDimension = new int[numberOfCovens];
		Coven[] coven =  new Coven[numberOfCovens];
		Thread[] witches = new Thread[numberOfWitches];
		IPotionQueue[] potionQueues = new IPotionQueue[numberOfCovens];
		DemonSpawner demonSpawner = new DemonSpawner(coven, numberOfCovens);

		DemonStopper demonStopper = new DemonStopper();
		for(int i = 0; i < numberOfCovens; i++) {
			potionQueues[i] = new PotionQueue();
		}
		for(int i = 0; i < numberOfCovens; i++) {
			covenDimension[i] = rand.nextInt(MAXIMUM_COVEN_DIMENSION - MINIMUM_COVEN_DIMENSION) + MINIMUM_COVEN_DIMENSION;
			coven[i] = new Coven(i, covenDimension[i], potionQueues[i]);
			coven[i].start();
		}
		for(int i = 0; i < numberOfWitches; i++) {
			witches[i] = new Witch(i, potionQueues, numberOfCovens);
			witches[i].start();
		}

		demonSpawner.start();
		demonStopper.start();

		for(int i = 0; i < numberOfCovens; i++) {
			try {
				coven[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Could not join coven " + i);
			}
		}
		try {
			demonSpawner.join();
		} catch (InterruptedException e) {
			System.out.println("Could not join demon spawner");
			e.printStackTrace();
		}
		try {
			demonStopper.join();
		} catch (InterruptedException e) {
			System.out.println("Could not join  demon stopper");
			e.printStackTrace();
		}
		for(int i = 0; i < numberOfWitches; i++) {
			try {
				witches[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Could not join witches " + i);
			}
		}
		System.out.println("Application finished");
	}

}
