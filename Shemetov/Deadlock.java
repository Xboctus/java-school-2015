import java.util.concurrent.locks.ReentrantLock;


public class Deadlock {
	
	public static String first = "dog";
	public static String second = "cat";
	
	public static void main(String[] args) {
		FirstThread thread1 = new FirstThread();
		SecondThread thread2 = new SecondThread();
		thread1.start();
		thread2.start();
	}

}


class FirstThread extends Thread {
	private final ReentrantLock lock = new ReentrantLock();
	public void run() {
		synchronized(Deadlock.first){
			System.out.println("firstThread captured a " + Deadlock.first);
			lock.lock();
			synchronized(Deadlock.second){
				System.out.println("FirstThread is OK");
			}
		}
	}
}

class SecondThread extends Thread {
	private final ReentrantLock lock = new ReentrantLock();
	public void run() {
		synchronized(Deadlock.second){
			System.out.println("secondThread captured a " + Deadlock.second);
			lock.lock();
			synchronized(Deadlock.first){
				System.out.println("SecondThread is OK");
			}
		}
	}
}
