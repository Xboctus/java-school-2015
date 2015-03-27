import java.util.*;

public class EventTimer {
	private static Timer timer;

	public static void startTimer() {
		timer = new Timer();
	}

	public static void stopTimer() {
		timer.cancel();
	}
}
