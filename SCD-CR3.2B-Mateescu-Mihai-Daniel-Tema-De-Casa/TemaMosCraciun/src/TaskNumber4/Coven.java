package TaskNumber4;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class Coven extends Thread {
	/**
	 * coven identifier
	 */
	int id;
	/**
	 * coven size
	 */
	public int covenDimensions;
	/**
	 * potion queue common with demons and witches
	 */
	IPotionQueue potionQueue;
	/**
	 * maximum number of demons that can be spawned in a coven
	 */
	public int maxDemons;
	/**
	 * vector of demons in coven
	 */
	Vector<Demon> demon;
	/**
	 * current number of demons in coven
	 */
	public int currentNumberOfDemons = 0;
	/**
	 * barrier that will be injected to all demons in coven
	 */
	HomeMadeCyclicBarrier barrier;
	/**
	 * hash map of demons and their position in coven
	 */
	HashMap<String, Demon> demonPositions = new HashMap<String, Demon>();
	/**
	 * @param id coven identifier
	 * @param covenDimension coven size
	 * @param potionQueue queue common with demons and witches
	 */
	Coven(int id, int covenDimension, IPotionQueue potionQueue){
		this.id = id;
		this.covenDimensions = covenDimension;
		this.potionQueue = potionQueue;
		maxDemons = covenDimension/2;
		demon = new Vector<Demon>(maxDemons);
		barrier = new HomeMadeCyclicBarrier(covenDimension);
		System.out.println("Created coven " + id + " with maximum no of demons: "+ maxDemons);
	}
	/**
	 * Method checks if there is enough space to add another demon. If so, it generates a random position until
	 * it gets a valid one and places the demon in that position.
	 */
	public synchronized void addDemonToCoven() {
		if(currentNumberOfDemons < maxDemons) {
			Random rand = new Random();
			int x = rand.nextInt(covenDimensions);
			int y = rand.nextInt(covenDimensions);
			while(demonPositions.get(x + " " + y) != null) {
				x = rand.nextInt(covenDimensions);
				y = rand.nextInt(covenDimensions);
			}
			Position pos = new Position(x,y);
			demon.add(currentNumberOfDemons, new Demon(pos , covenDimensions, demonPositions, potionQueue,id, barrier));
			demonPositions.put((pos.x + " " + pos.y), demon.get(currentNumberOfDemons));
			demon.get(currentNumberOfDemons).start();
			currentNumberOfDemons++;
			System.out.println("Added demon to coven " + this.id + " at position " + pos.x + " " + pos.y + " current count: " + currentNumberOfDemons);
		}
	}
	
	/**
	 * Asks the demons for their positions
	 */
	public void run() {
		while(Demon.demonCurrentNumberOfPotions < Application.NO_OF_POTIONS_TO_ACHIEVE) {
			for(int i = 0; i < currentNumberOfDemons; i++) {
				demon.get(i).getPosition();
			}
		}
		System.out.println("Coven " + id + " finished");
	}
}
