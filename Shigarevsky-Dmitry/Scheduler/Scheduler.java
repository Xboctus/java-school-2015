import java.util.*;

public class Scheduler {
	private static void printUsage() {
		System.err.println("Usage: java Scheduler <ui_type> , where <ui_type>");
		System.err.println("cli - Command Line Interface");
		System.err.println("gui - Graphical User Interface");
	}

	private static void init() {
		Date now = new Date();

		Coordinator.createUser("user", TimeZone.getTimeZone("GMT+5"), true);
		Coordinator.addGlobalEvent("user", "fastEvent", new Date(now.getTime() + 1000*10));
		Coordinator.addGlobalEvent("user", "slowEvent", new Date(now.getTime() + 1000*20));
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			printUsage();
			return;
		}
		if (args[0].equals("cli")) {
			init();
			Cli.run();
		} else if (args[0].equals("gui")) {
			init();
			Gui.run();
		} else {
			printUsage();
		}
	}
}
