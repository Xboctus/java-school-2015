// TODO: multiplexing + thread pooling

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;

class ConnectionDispatcher extends Thread {
	// numbered members are assumed to be accesses by Server in the respective order

	// 1. Initialize connection dispatcher
	static void init() throws IOException {
		conDispatcher = new ConnectionDispatcher();
		conDispatcher.start();
	}

	// 2. Get event port
	static int eventPort = 0;

	// 3. Finalize connection dispatcher
	static void fin() throws InterruptedException {
		if (conDispatcher == null) {
			return;
		}

		try {
			conDispatcher.softInterrupt();
		} catch (Exception e) {
			finExceptions.add(e);
		}

		try {
			conDispatcher.join();
		} catch (InterruptedException e) {
			// NOTE: dispatcher can still be running at this point
			// FIXME: try to salvage exceptions anyway
			finExceptions.add(new Exception("Some exceptions may be omitted due to interruption"));
			conDispatcher = null;
			throw e;
		}

		finExceptions.addAll(conDispatcher.runExceptions);
		conDispatcher.runExceptions = null;
		if (conDispatcher.runInterruptedExc != null) {
			finExceptions.add(conDispatcher.runInterruptedExc);
			conDispatcher.runInterruptedExc = null;
		}

		conDispatcher = null;
	}

	// 4. Get exceptions thrown during finalization
	static final ArrayList<Exception> finExceptions = new ArrayList<>();


	// the instance of this singleton class
	private static ConnectionDispatcher conDispatcher = null;

	private ConnectionDispatcher() throws IOException {
		super("Connection dispatcher");
		this.serverSocket = new ServerSocket(0);
		eventPort = serverSocket.getLocalPort();
	}

	private ServerSocket serverSocket;

	// fn#1
	private void softInterrupt() throws IOException {
		serverSocket.close();
	}

	private ArrayList<Exception> runExceptions = new ArrayList<>();
	private Exception runInterruptedExc = null;

	@Override
	public void run() {
		ArrayList<ConnectionHandler> conHandlers = new ArrayList<>();

		while (true) {
			ConnectionHandler ch;
			try {
				ch = new ConnectionHandler(serverSocket.accept(), conHandlers); // fn#2
			} catch (SocketException e) { // from serverSocket.accept() because of softInterrupt()
				break;
			} catch (IOException e) {
				runExceptions.add(e);
				break; // TODO: maybe we can continue?..
			}
			conHandlers.add(ch);
			ch.start();
		}

		serverSocket = null;

		synchronized (conHandlers) {
			for (ConnectionHandler ch: conHandlers) {
				try {
					ch.softInterrupt();
				} catch (Exception e) {
					runExceptions.add(e);
				}
			}
		}

		// iteratively pick any handler from the list and wait for it to end
		// (at the end it removes itself from the list so loop will end eventually)
		while (true) {
			ConnectionHandler ch;
			synchronized (conHandlers) {
				if (conHandlers.isEmpty()) {
					break;
				}
				ch = conHandlers.get(0);
			}
			try {
				ch.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // restore interrupted status
				runInterruptedExc = e;
				break; // prevent further blocking
			}
			runExceptions.addAll(ch.runExceptions);
			ch.runExceptions = null;
		}

		conHandlers = null;
	}
}

// fn#1
// reasons to "interrupt" dispatcher by closing its socket rather than just interupt()ing it:
// a. interrupt BEFORE serverSocket.accept() seems not to make it fail
// b. interrupt DURING serverSocket.accept() seems not to make it fail either
//
// better to implement and use this method rather than override interrupt() because:
// a. specification of interrupt() is not compatible with what we want to do (see above)
// b. interrupt() doesn't support throwing, so we would need a RuntimeException wrapper

// fn#2
// 'socket = serverSocket.accept()' would make Eclipse complain about a potential resource leak
