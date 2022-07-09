package TaskNumber4;

import java.util.LinkedList;
import java.util.Queue;

class PotionQueue implements IPotionQueue {

	/**
	 * Potion queue
	 */
	Queue<Potion> queueLock = new LinkedList<Potion>();

	/**
	 * variable that keeps track if the demon is writing in queue or not
	 */
	boolean isDemonPushingPotion = false;


	/**
	 * adds a potion to the queue
	 * @param value potion to be added in queue
	 */
	public void push(Potion value) {
		synchronized (queueLock) {
			isDemonPushingPotion = true;
			queueLock.add(value);
			queueLock.notify();
			System.out.println("Demon added in queue potion at position " + value.position.x + " " + value.position.y);
			isDemonPushingPotion = false;
		}
	}

	/**
	 * Removes the potion from queue if none is writing, otherwise returns null
	 */

	public Potion pop() {
		Potion item = null;
		synchronized (queueLock) {
			if (!isDemonPushingPotion) {
				item = queueLock.poll();
				if (null == item ) {
					return item;
				}
			}
			System.out.println( "Witch received potion at position " + item.position.x + " " + item.position.y);
			return item;
		}
	}
}