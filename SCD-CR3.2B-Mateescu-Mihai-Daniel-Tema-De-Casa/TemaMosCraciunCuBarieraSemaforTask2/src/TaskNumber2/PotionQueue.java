package TaskNumber2;

import java.util.LinkedList;
import java.util.Queue;

class PotionQueue implements IPotionQueue {
	
	Queue<Potion> queueLock = new LinkedList<Potion>();
	boolean isDemonPushingPotion = false;

	public void push(Potion value) {
		synchronized (queueLock) {
			isDemonPushingPotion = true;
			queueLock.add(value);
			queueLock.notify();
			System.out.println("Demon added in queue potion at position " + value.position.x + " " + value.position.y);
			isDemonPushingPotion = false;
		}
	}
	
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