public class Scheduler {
	private static void printUsage() {
		System.err.println("Usage: java Scheduler <ui_type> , where <ui_type>");
		System.err.println("cli - Command Line Interface");
		System.err.println("gui - Graphical User Interface");
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			printUsage();
			return;
		}
		if (args[0].equals("cli")) {
			Cli.run();
		} else if (args[0].equals("gui")) {
			Gui.run();
		} else {
			printUsage();
			return;
		}
	}
}
