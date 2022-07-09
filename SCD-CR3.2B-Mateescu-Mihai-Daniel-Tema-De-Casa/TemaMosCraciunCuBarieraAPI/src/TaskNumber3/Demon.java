package TaskNumber3;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demon extends Thread{

	public static volatile int demonCurrentNumberOfPotions = 0;
	public static Lock potionLock = new ReentrantLock();
	public static Lock moveLock = new ReentrantLock();
	private Position position;
	public IPotionQueue potionQueue;
	public int covenSize;
	HashMap<String, Demon> demonMapPosition;
	public int covenId;
	public CyclicBarrier barrier;

	Demon(Position position, int covenSize, HashMap<String, Demon> demonMapPosition, IPotionQueue potionQueue, int covenId, CyclicBarrier barrier){
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

	private void checkIfOnDiagonalAndRestOrJump() {
        Random rand = new Random();
		int choice = rand.nextInt(2);
        if (this.position.x == this.position.y) {
            if (choice == 0) {
				System.out.println("Demon at position " + position.x + " " + position.y + " is at barrier");
                try {
					barrier.await();
				} catch (InterruptedException | BrokenBarrierException e) {
					e.printStackTrace();
				}
            }else {
            	moveDemon();
            	checkIfOnDiagonalAndRestOrJump();
            }
        }
    }

	public void run() {
		while((Demon.demonCurrentNumberOfPotions < Application.NO_OF_POTIONS_TO_ACHIEVE)) {
			moveDemon();
			checkIfOnDiagonalAndRestOrJump();
			tryToStopDemon();
		}
		System.out.println("Demon in coven " + covenId + " finished");
	}

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

	private boolean canMoveRight() {
		if(position.x + 1 < covenSize)
			if(demonMapPosition.get((position.x + 1) + " " + position.y) == null) {
				return true;
			}
		return false;
	}

	private void moveRight() {
		removeOldPosition();
		System.out.println("Demon in coven " + covenId + " moved right from " + this.position.x + " " + this.position.y);
		this.position.x += 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}

	private boolean canMoveUp() {
		if(position.y - 1 > 0)
			if(demonMapPosition.get(position.x + " " + (position.y - 1)) == null) {
				return true;
			}
		return false;
	}

	private void moveUp() {
		removeOldPosition();
		System.out.println(" Demon in coven " + covenId + " moved up from " + this.position.x + " " + this.position.y);
		this.position.y -= 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}

	private boolean canMoveDown() {
		if(position.y + 1 < (covenSize))
			if(demonMapPosition.get(position.x + " " + (position.y + 1)) == null) {
				return true;
			}
		return false;
	}

	private void moveDown() {
		removeOldPosition();
		System.out.println(" Demon in coven " + covenId + " moved down from " + this.position.x + " " + this.position.y);
		this.position.y += 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}

	private boolean canMoveLeft() {
		if(position.x - 1 > 0)
			if(demonMapPosition.get((position.x - 1) + " " + position.y) == null) {
				return true;
			}
		return false;
	}

	private void moveLeft() {
		removeOldPosition();
		System.out.println(" Demon in coven " + covenId + " moved left from " + this.position.x + " " + this.position.y);
		this.position.x -= 1;
		addNewPosition();
		createPotion();
		restAfterCreatingPotion();
	}

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

	private void restAfterCreatingPotion() {
		try {
			System.out.println("Demon in coven " + covenId + " created potion and is resting current count: "+ Demon.demonCurrentNumberOfPotions);
			Thread.sleep(30);
		} catch (InterruptedException e) {
			System.out.println("Demon could not sleep after creating potion ");
			e.printStackTrace();
		}
	}

	private synchronized void createPotion() {
		potionLock.lock();
		potionQueue.push(new Potion(new Position(position.x, position.y)));
		Demon.demonCurrentNumberOfPotions++;
		potionLock.unlock();
	}

	private synchronized void addNewPosition() {
		moveLock.lock();
		demonMapPosition.put(position.x + " " + position.y, this);
		moveLock.unlock();
	}

	private synchronized void removeOldPosition() {
		moveLock.lock();
		demonMapPosition.remove(position.x + " " + position.y);
		moveLock.unlock();
	}
}
