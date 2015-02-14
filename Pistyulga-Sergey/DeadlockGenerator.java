
public class DeadlockGenerator {
	private static boolean thread1Finished = false,
			thread2Finished = false;
	
	private static void catchFinishedThread1() {
		thread1Finished = true;
	}
	
	private static void catchFinishedThread2() {
		thread2Finished = true;
	}
	
	public static void main(String[] args) {
		
		final Object resourceA = new Object(),
				resourceB = new Object();
		
		final Thread thread1 = new Thread(new Runnable() {
			
			public void run() {
				synchronized (resourceB) {
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {}
					}
					synchronized (resourceA) {}
				}
				catchFinishedThread1();
			}
		}),
		thread2 = new Thread(new Runnable() {
			
			public void run() {
				synchronized (resourceA) {
					synchronized (thread1) {
						thread1.notify();
					}
					synchronized (resourceB) {}
				}
				catchFinishedThread2();
			}
		});
		
		thread1.start(); thread2.start();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
		if (!(thread1Finished && thread2Finished))
			System.out.println("Deadlock is reached.");
		System.exit(0);
	}

}
