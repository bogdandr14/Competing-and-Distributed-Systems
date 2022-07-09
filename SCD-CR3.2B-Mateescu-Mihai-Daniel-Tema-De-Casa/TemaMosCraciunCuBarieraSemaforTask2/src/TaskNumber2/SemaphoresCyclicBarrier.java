package TaskNumber2;
/**
 *	Implementation of barrier using semaphores. The implementation is made according to
 *	"the little book of semaphores" by Allen B. Downey
 *	http://greenteapress.com/semaphores/LittleBookOfSemaphores.pdf  page 41
 */

public class SemaphoresCyclicBarrier {
	/**
	 *	number of demons that must arrive at the barrier in order to release everyone
	 */
	private int n;
	/**
	 *	current number of demons at the barrier
	 */
	private int count;
	private Semaphore turnstile ;
	private Semaphore turnstile2 ;
	private Semaphore mutex ;
	
	public SemaphoresCyclicBarrier(int n) {
		this.n = n;
		this.count = 0;
		this.mutex = new Semaphore(1);
		this.turnstile = new Semaphore(0);
		this.turnstile2 = new Semaphore(0);
	}
	
	public void await() {
		this.phase1();
		this.phase2();
	}

	private void phase2() {
		this.mutex.down();
		this.count--;
		if(this.count == 0) {
			for(int i = 0; i < n; i++) {
				this.turnstile2.signal();
			}
		}
		this.mutex.signal();
		System.out.println("Demons were released");
		this.turnstile2.down();
		
	}

	private void phase1() {
		this.mutex.down();
		this.count++;
		if(this.count == this.n) {
			for(int i = 0; i < n; i++) {
				this.turnstile.signal();
			}
		}
		this.mutex.signal();
		this.turnstile.down();
	}
	
	public class Semaphore{
		private int counter;
		public Semaphore(int number) {
			if(number>0) {
				this.counter = number;
			}
		}
		
		public synchronized void signal(){
			this.counter++;
			notifyAll();
		}
		
		public synchronized void down(){
			while(this.counter <= 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.counter--;
		}
	}
}
