package TaskNumber4;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of a barrier with locks
 */

public class HomeMadeCyclicBarrier {
    private final int parties;
    private final Lock lock = new ReentrantLock();;
    private int arrived = 0;

    /**
     * Constructor. Initializes fields
     * @param parties the total demons that must wait
     */

    public HomeMadeCyclicBarrier(int parties) {
        this.parties = parties;
    }

    /**
     * If an demon calls await method he will have to wait until other n-1 demons call it or the app stops
     */

    public void await() {
        try {
            lock.lock();
            arrived++;
            doWait();
        } finally {
            lock.unlock();
        }
    }

    /**
     * This is the function that does the actual waiting. When it finishes, it resets the barrier.
     */

    private synchronized void doWait() {
        while (arrived != parties && Application.CURRENT_NO_OF_POTIONS < Application.NO_OF_POTIONS_TO_ACHIEVE) {
            try {
                wait(100, 100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        notifyAll();
        arrived = 0;
    }
}
