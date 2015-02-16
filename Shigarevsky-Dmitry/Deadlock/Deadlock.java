import java.util.*;
import java.util.concurrent.locks.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.io.*;

public class Deadlock {
	private static long idm;
	private static final long[] ids = new long[2];

	private static class OutputEntry {
		public final LocalTime time;
		public final long threadId;
		public final String threadName;
		public final String object;
		public final String action;

		public OutputEntry(LocalTime time, long threadId, String threadName, String object, String action) {
			this.time = time;
			this.threadId = threadId;
			this.threadName = threadName;
			this.object = object;
			this.action = action;
		}

		private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

		public void print(PrintWriter out) {
			out.format("[%s] (%s)", time.format(dtf), threadName);
			int indent = (
				threadId == idm ? 1 :
				threadId == ids[0] ? 6 :
				threadId == ids[1] ? 11 : 16
			);
			for (int i = 0; i < indent; ++i) {
				out.print(" ");
			}
			out.format("%-5s", object);
			indent = 17 - indent;
			for (int i = 0; i < indent; ++i) {
				out.print(" ");
			}
			out.println(action);
		}
	}

	private static final Vector<OutputEntry> output = new Vector<OutputEntry>();

	private static void addOutputEntry(String object, String action) {
		output.add(new OutputEntry(
			LocalTime.now(),
			Thread.currentThread().getId(),
			Thread.currentThread().getName(),
			object, action
		));
	}

	private static void printOutput() {
		PrintWriter out;
		try {
			out = new PrintWriter("output.txt");
		} catch (FileNotFoundException e) {
			System.err.println("output.txt cannot be written");
			System.exit(-1);
			/*no*/return;
		}
		for (OutputEntry oe: output) {
			oe.print(out);
		}
		out.close();
	}

	private static class NamedAndLocked<T> {
		public T val;
		public final String name;
		public final ReentrantLock lock;
		public final Condition cond;

		NamedAndLocked(T val, String name) {
			this.val = val;
			this.name = name;
			this.lock = new ReentrantLock();
			this.cond = this.lock.newCondition();
		}

		void lock() {
			addOutputEntry(name, "|lock");
			lock.lock();
			addOutputEntry(name, "lock|");
		}

		void unlock() {
			addOutputEntry(name, "|unlock");
			lock.unlock();
			addOutputEntry(name, "unlock|");
		}
	}

	private static class Resource {
		public final NamedAndLocked<Integer> data;
		public final NamedAndLocked<Boolean> ready;

		public Resource(int n) {
			this.data = new NamedAndLocked<Integer>(0, "x" + (n + 1));
			this.ready = new NamedAndLocked<Boolean>(false, "r" + (n + 1));
		}
	}

	private static class MyThread extends Thread {
		public final int n;
		public final Resource own;

		private static final long RUN_MAX_DELAY = 0;

		public static final MyThread[] threads = new MyThread[2];

		private static final Random random = new Random();

		MyThread(final int n) {
			super("thr" + (n + 1));
			this.n = n;
			this.own = new Resource(n);

			ids[n] = this.getId();
		}

		public void run() {
			addOutputEntry("run", "{|");

			addOutputEntry("run", "|sleep");
			try {
				Thread.sleep(Math.abs(random.nextLong())%(RUN_MAX_DELAY + 1), random.nextInt(1000*1000));
			} catch (InterruptedException e) {
				;
			}
			addOutputEntry("run", "sleep|");

			Resource other = threads[1 - n].own;

			own.data.lock();
				other.ready.lock();
					while (!other.ready.val) {
						try {
							other.ready.cond.await();
						} catch (InterruptedException e) {
						}
					}
				other.ready.unlock();
				other.data.lock();
					own.data.val = other.data.val + 1;
				other.data.unlock();
			own.data.unlock();

			own.ready.lock();
				own.ready.val = true;
				own.ready.cond.signal();
			own.ready.unlock();

			addOutputEntry("run", "|}");
		}

		public void start() {
			addOutputEntry(getName(), "|start");
			super.start();
			addOutputEntry(getName(), "start|");
		}
	}

	private static final long QUIT_DELAY = 2*1000;

	public static void main(String[] args) {
		idm = Thread.currentThread().getId();

		for (int i = 0; i < 2; ++i) {
			MyThread.threads[i] = new MyThread(i);
		}

		for (int i = 0; i < 2; ++i) {
			MyThread.threads[i].start();
		}

		final Timer timer = new Timer("timr");
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				timer.cancel();
				addOutputEntry("prog", "terminate");
				printOutput();
				System.exit(0);
			}
		}, QUIT_DELAY);

		for (int i = 2; i > 0; --i) {
			try {
				MyThread.threads[i - 1].join();
			} catch (InterruptedException e) {
				System.err.format("Interrupted waiting on thread #%d!", i);
			}
		}

		timer.cancel();
		printOutput();
	}
}
