import java.util.concurrent.*;

class ParallelCalc implements Callable<Character> {
	
	private int digitNumber;
	
	private static ExecutorService executor;
	
	public static void initThreadPool(int maxThreadsCount) {
		if (executor!=null) executor.shutdown();
		executor = Executors.newFixedThreadPool(maxThreadsCount);
	}
	
	public static Future<Character> getTask(int digitNum) {
		return executor.submit(new ParallelCalc(digitNum));
	}
	
	ParallelCalc(int digitNum) {
		digitNumber = digitNum;
	}
	
	private double modPow(double x, int n, double p) {
		double y = x % p, z = 1;
		while (n%2==0) {
			y = (y * y) % p;
			n /= 2;
		}
		while (n>1) {
			z = (z * y) % p;
			n--;
			while (n%2==0) {
				y = (y * y) % p;
				n /= 2;
			}
		}
		return (y*z) % p;
	}
	
	public char getHexDigit() {
		double res = 0, s1 = 0, s2 = 0, s3 = 0, s4 = 0;
		for (int k = 0; k < digitNumber; k++) {
			double p1 = 8*k + 1, p2 = 8*k + 4, p3 = 8*k + 5,
					p4 = 8*k + 6;
			s1 += modPow(16, digitNumber-k, p1)/p1;
			s2 += modPow(16, digitNumber-k, p2)/p2;
			s3 += modPow(16, digitNumber-k, p3)/p3;
			s4 += modPow(16, digitNumber-k, p4)/p4;
			s1 -= Math.floor(s1);
			s2 -= Math.floor(s2);
			s3 -= Math.floor(s3);
			s4 -= Math.floor(s4);
			
		}
		for (int k = 0; ; k++) {
			double p1 = 8*(digitNumber+k) + 1, p2 = 8*(digitNumber+k)  + 4,
					p3 = 8*(digitNumber+k)  + 5, p4 = 8*(digitNumber+k)  + 6;
			double d = Math.pow(16, -k)/p1;
			if (d<1e-5) break;
			s1 += d;
			s2 += Math.pow(16, -k)/p2;
			s3 += Math.pow(16, -k)/p3;
			s4 += Math.pow(16, -k)/p4;
			s1 -= Math.floor(s1);
			s2 -= Math.floor(s2);
			s3 -= Math.floor(s3);
			s4 -= Math.floor(s4);
		}
		res += (4*s1-2*s2-s3-s4);
		res = Math.abs(res - Math.floor(res));
		char hexdigit;
		res = 16. * (res - Math.floor (res));
		hexdigit = Integer.toHexString((int)res).charAt(0);
		return hexdigit;
	}
	
	public Character call() throws Exception {
		return getHexDigit();
	}
	
}

public class PICalcMT {
	private static String usage = "Usage: <program> <digits_count> <threads_count> [multirun]";
	
	private static void abort(String message) {
		System.out.println(message);
		System.exit(1);
	}
	
	public static void main(String[] args) {
		int digitsCount = 0, threadsCount = 0;
		boolean isMax = false;
		if (args.length>=1) {
			try {
				digitsCount = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e) {
				abort("Incorrect number of digits!");
			}
			if (args.length>=2) {
				try {
					threadsCount = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException e) {
					abort("Incorrect number of threads!");
				}
				if (args.length==3 && args[2].equals("multirun"))
					isMax = true;
				else if (args.length>=3) {
					abort(usage);
				}
			}
			else abort(usage);
		}
		else abort(usage);
		
		String answer = "";
		Future<Character>[] tasks = new Future[digitsCount];
		System.out.println("Calculating "+digitsCount+" digits");
		int startThreadsNum = (isMax) ? 1 : threadsCount;
		for (int i = startThreadsNum; i <= threadsCount; i++) {
			long startTime = System.currentTimeMillis();
			ParallelCalc.initThreadPool(i);
			for (int j = 0; j < digitsCount; j++)
				tasks[j] = ParallelCalc.getTask(j);
			answer = "";
			for (Future<Character> task : tasks)
				try {
					answer += task.get();
				} catch (InterruptedException e) {}
				catch (ExecutionException e) {}
			System.out.println("Threads: "+i+"; Work time: "+
						(System.currentTimeMillis()-startTime));
		}
		//System.out.println("Answer: "+answer);
		System.exit(0);
	}
}
