package TaskNumber2;

import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

public class Coven extends Thread {

	int id;
	public int covenDimensions;
	IPotionQueue potionQueue;
	public int maxDemons;
	Vector<Demon> demon;
	public int currentNumberOfDemons = 0;
	SemaphoresCyclicBarrier barrier;
	HashMap<String, Demon> demonPositions = new HashMap<String, Demon>();

	Coven(int id, int otherCovenDimensions, IPotionQueue potionQueue){
		this.id = id;
		this.covenDimensions = otherCovenDimensions;
		this.potionQueue = potionQueue;
		maxDemons = otherCovenDimensions / 2;
		demon = new Vector<Demon>(maxDemons);
		barrier = new SemaphoresCyclicBarrier(otherCovenDimensions);
		System.out.println("Created coven " + id + " with maximum no of demons: "+ maxDemons);
	}
	
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

	public void run() {
		while(Demon.demonCurrentNumberOfPotions < Application.NO_OF_POTIONS_TO_ACHIEVE) {
			for(int i = 0; i < currentNumberOfDemons; i++) {
				demon.get(i).getPosition();
			}
		}
		System.out.println("Coven " + id + " finished");
	}
}
