package TaskNumber4;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demon extends Thread{
	/**
	 * current number of potions created by demons. When this is equal with the goal, all threads except witches can stop working
	 */
	public static volatile int demonCurrentNumberOfPotions = 0;
	/**
	 * a lock used to ensure exclusive access to the number o potions variable
	 */
	public static Lock potionLock = new ReentrantLock();
	/**
	 * a lock used to ensure exclusive access to the demon positions in a map
	 */
	public static Lock moveLock = new ReentrantLock();
	/**
	 * position on x and y coordinated of an demon in a coven
	 */
	private Position position;
	/**
	 * queue of potions; common with a coven, demons and witches
	 */
	public IPotionQueue potionQueue;
	/**
	 * dimmension of the matrix coven the demon belongs to
	 */
	public int covenSize;/**
	 * map that contains demon demons positions
	 */
	HashMap<String, Demon> demonMapPosition;
	/**
	 * identifier of the coven the demon belongs to
	 */
	public int covenId;
	/**
	 * cyclic barrier used to make demons wait when they are on the main diagonal until there are demons on all positions of the main diagonal
	 */
	public HomeMadeCyclicBarrier barrier;
	/**
	 * @param position Coordinates x and y of demon in coven
	 * @param covenSize coven size
	 * @param demonMapPosition Map that has the coordinates of all demons in coven
	 * @param potionQueue queue where demons write potions
	 * @param covenId coven identifier
	 * @param barrier cyclic barrier used to make demons wait when they are on the main diagonal until there are demons on all positions of the main diagonal
	 */
	Demon(Position position, int covenSize, HashMap<String, Demon> demonMapPosition, IPotionQueue potionQueue, int covenId, HomeMadeCyclicBarrier barrier){
		this.position = position;
		this.covenSize = covenSize;
		this.demonMapPosition = demonMapPosition;
		this.potionQueue = potionQueue;
		this.covenId = covenId;
		this.barrier = barrier;
	}

	public Position getPosition() {
		return this.position;
	}
	/**
	 * Checks if the demon moved on the main diagonal. If so, generate a random value 1 or 0 in order to
	 * choose if the demon waits at the barrier or makes another move.
	 * If the choice is 1, call await method of the barrier
	 */
	private void checkIfOnDiagonalAndRestOrJump() {
		Random rand = new Random();
		int choice = rand.nextInt(2);
		if (this.position.x == this.position.y) {
			if (choice == 0) {
				System.out.println("Demon at position " + position.x + " " + position.y + " is at barrier");
				barrier.await();
			}else {
				moveDemon();
				checkIfOnDiagonalAndRestOrJump();
			}
		}
    }
	/**
	 * Main method called when thread starts
	 * Move demon, check if it is on diagonal and try to stop demon
	 */
	public void run() {
		while((Demon.demonCurrentNumberOfPotions < Application.NO_OF_POTIONS_TO_ACHIEVE)) {
			moveDemon();
			checkIfOnDiagonalAndRestOrJump();
			tryToStopDemon();
		}
		System.out.println("Demon in coven " + covenId + " finished");
		
	}

	/**
	 * try to acquire a semaphore permit for an amount of time and if you succeed the demon enters a finite retirement state
	 */
	private synchronized void tryToStopDemon() {
		try {
			if(DemonStopper.stopDemonSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS)){
				System.out.println("Demon in coven " + covenId + " is stopping");
				wait(1000,1000);
				System.out.println("Demon in coven " + covenId + " is restarting");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * check if the demon can move in a direction and if so move, otherwise sleep
	 */
	private void moveDemon() {
		if(canMoveRight()) {
			moveRight();
		}else if(canMoveUp()) {
			moveUp();
		}else if(canMoveDown()) {
			moveDown();
		}else if(canMoveLeft()) {
			moveLeft();
		}else {
			restBecauseYouAreSurrounded();
		}
	}
	/**
	 * Checks if the demon can move right, i.e. there isn't another demon there and the demon is still within the coven boundaries
	 * @return true if he can move, false otherwise 
	 */
	private boolean canMoveRight() {
		if(position.x + 1 < covenSize)
			if(demonMapPosition.get((position.x + 1) + " " + position.y) == null) {
				return true;
			}
		return false;
	}
	/**
	 * update position and call methods to increment number of potions and to rest
	 */
	private void moveRight() {
		removeOldPosition();
		System.out.println("Demon in coven " + covenId + " moved right from " + this.position.x + " " + this.position.y);
		this.position.x += 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}
	/**
	 * Checks if the demon can move up, i.e. there isn't another demon there and the demon is still within the coven boundaries
	 * @return true if he can move, false otherwise 
	 */
	private boolean canMoveUp() {
		if(position.y - 1 > 0)
			if(demonMapPosition.get(position.x + " " + (position.y - 1)) == null) {
				return true;
			}
		return false;
	}
	/**
	 * update position and call methods to increment number of potions and to rest
	 */
	private void moveUp() {
		removeOldPosition();
		System.out.println(" Demon in coven " + covenId + " moved up from " + this.position.x + " " + this.position.y);
		this.position.y -= 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}

	/**
	 * Checks if the demon can move down, i.e. there isn't another demon there and the demon is still within the coven boundaries
	 * @return true if he can move, false otherwise 
	 */
	private boolean canMoveDown() {
		if(position.y + 1 < (covenSize))
			if(demonMapPosition.get(position.x + " " + (position.y + 1)) == null) {
				return true;
			}
		return false;
	}
	/**
	 * update position and call methods to increment number of potions and to rest
	 */
	private void moveDown() {
		removeOldPosition();
		System.out.println(" Demon in coven " + covenId + " moved down from " + this.position.x + " " + this.position.y);
		this.position.y += 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}

	/**
	 * Checks if the demon can move left, i.e. there isn't another demon there and the demon is still within the coven boundaries
	 * @return true if he can move, false otherwise 
	 */
	private boolean canMoveLeft() {
		if(position.x - 1 > 0)
			if(demonMapPosition.get((position.x - 1) + " " + position.y) == null) {
				return true;
			}
		return false;
	}
	/**
	 * update position and call methods to increment number of potions and to rest
	 */
	private void moveLeft() {
		removeOldPosition();
		System.out.println(" Demon in coven " + covenId + " moved left from " + this.position.x + " " + this.position.y);
		this.position.x -= 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}
	
	/**
	 * generate a random number and sleep thread
	 */
	private void restBecauseYouAreSurrounded() {
		Random rand = new Random();
		int max_sleep_time = 50;
		int min_sleep_time = 10;
		int sleep_time = rand.nextInt(max_sleep_time - min_sleep_time) + min_sleep_time;
		try {
			System.out.println("Demon in coven " + covenId + " at position " + this.position.x + " " + this.position.y + " is surrounded and is resting");
			Thread.sleep(sleep_time);
		} catch (InterruptedException e) {
			System.out.println("Demon could not rest although it's surrounded");
			e.printStackTrace();
		}
	}
	/**
	 * generate a random number and sleep thread
	 */
	private void restAfterCreatingPotion() {
		try {
			System.out.println("Demon in coven " + covenId + " created potion and is resting current count: "+ Demon.demonCurrentNumberOfPotions);
			Thread.sleep(30);
		} catch (InterruptedException e) {
			System.out.println("Demon could not sleep after creating potion ");
			e.printStackTrace();
		}
	}
	/**
	 * increment number of potions
	 */
	private synchronized void createPotion() {
		potionLock.lock();
		potionQueue.push(new Potion(new Position(position.x, position.y)));
		Demon.demonCurrentNumberOfPotions++;
		potionLock.unlock();
	}
	/**
	 * update demon position in map
	 */
	private synchronized void addNewPosition() {
		moveLock.lock();
		demonMapPosition.put(position.x + " " + position.y, this);
		moveLock.unlock();
	}
	/**
	 * remove from map the position where there no longer exists an demon
	 */
	private synchronized void removeOldPosition() {
		moveLock.lock();
		demonMapPosition.remove(position.x + " " + position.y);
		moveLock.unlock();
	}
}
