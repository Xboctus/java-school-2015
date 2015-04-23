import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler extends Thread {
	private enum InterruptCause {
		NONE,
		IO_ERROR, // io error, write possible
		WRITE_FAIL, // io error, write impossible
		SHUTDOWN, // soft interrupt from connection dispatcher
		DISCONNECT, // disconnect command from client
	}

	private List<ConnectionHandler> handlers;

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer = null;
	private volatile InterruptCause interruptCause = InterruptCause.NONE;
	private Exception ctorExc = null;

	ConnectionHandler(Socket socket, List<ConnectionHandler> handlers) {
		super("Connection handler for " + socket.getRemoteSocketAddress());

		try {
			// NOTE: initializing writer first allows notifying about failed reader initialization
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			ctorExc = e;
			// just construct in the interrupted state
			interruptCause = InterruptCause.IO_ERROR;
		}

		this.handlers = handlers;
		this.socket = socket;
	}

	// writeException is synchronized on writer
	private IOException writeException = null;

	private void exclusiveWrite(String... parts) {
		synchronized (writer) {
			if (writeException != null) {
				return;
			}
			try {
				Shared.putParts(writer, parts);
			} catch (IOException e) {
				writeException = e;
				interruptCause = InterruptCause.WRITE_FAIL;
			}
		}
	}

	// this method is analogous to ConnectionDispatcher.softInterrupt()
	public void softInterrupt() throws IOException {
		interruptCause = InterruptCause.SHUTDOWN;
		socket.shutdownInput();
	}

	private static final String[] respPartsUnknownCommand = new String[] {"unknown_command"};
	private static final String[] respPartsInvalidArgsCount = new String[] {"invalid_args_count"};
	private static final String[] respPartsInternalError = new String[] {"internal_error"};

	ArrayList<Exception> runExceptions = new ArrayList<>();

	@Override
	public void run() {
		if (interruptCause == InterruptCause.NONE) {
			// XXX: (timer task can fail to write at this point)
			exclusiveWrite("connect_ack");
		}

		if (ctorExc != null) {
			runExceptions.add(ctorExc);
			ctorExc = null;
		}

		while (interruptCause == InterruptCause.NONE) {
			Shared.GetPartsResult r = Shared.getParts(reader);

			if (r.ioe != null || (r.endOfStream && interruptCause != InterruptCause.SHUTDOWN)) {
				interruptCause = InterruptCause.IO_ERROR;
				if (r.ioe != null) {
					runExceptions.add(r.ioe);
				}
				break;
			}

			String[] respParts = null;

			if (r.parts.length == 0) {
				respParts = respPartsUnknownCommand;
			} else {
				for (CommandHandler ch: commandHandlers) {
					if (r.parts[0].equals(ch.verb)) {
						if (r.parts.length - 1 != ch.argCount) {
							respParts = respPartsInvalidArgsCount;
						} else {
							try {
								respParts = ch.handle(this, r.parts);
							} catch (Exception e) {
								runExceptions.add(e);
								respParts = respPartsInternalError;
							}
						}
						break;
					}
				}
				if (respParts == null) {
					respParts = respPartsUnknownCommand;
				}
			}

			exclusiveWrite(respParts);
		}

		switch (interruptCause) {
		case IO_ERROR:
			if (this.writer != null) { // initialization of writer might fail (see ctor)
				exclusiveWrite("io_error");
			}
			break;
		case WRITE_FAIL:
			// no-op: handled below
			break;
		case DISCONNECT:
			// no-op: disconnect has already been acknowledged
			break;
		case SHUTDOWN:
			exclusiveWrite("shutdown");
			break;
		case NONE:
			assert(false);
		}

		if (interruptCause == InterruptCause.WRITE_FAIL) {
			runExceptions.add(writeException);
			writeException = null;
		}

		try {
			socket.close();
		} catch (IOException e) {
			runExceptions.add(e);
		}
		reader = null;
		writer = null;
		socket = null;

		synchronized (handlers) {
			handlers.remove(this); // uses equals() from Object which compares by reference
		}
		handlers = null;
	}

	private enum CommandStatement {
		LOGIN	("SELECT 1 FROM Users WHERE name = ? AND pass = ?");

		final String str;
		CommandStatement(String str) { this.str = str; }
	}

	public volatile String name = null;

	private static abstract class CommandHandler {
		String verb;
		int argCount;
		abstract String[] handle(ConnectionHandler ch, String[] parts) throws Exception;
	}

	private static class LoginHandler extends CommandHandler {
		{ verb = "login"; argCount = 2; }

		@Override
		public String[] handle(ConnectionHandler ch, String[] parts) throws Exception {
			boolean valid;
			try (
				PreparedStatement statement = DbConnector.createStatement(CommandStatement.LOGIN.str);
			) {
				statement.setString(1, parts[1]); // user name
				statement.setString(2, parts[2]); // password
				try (ResultSet rs = statement.executeQuery()) {
					valid = rs.next();
				}
			}
			if (!valid) {
				return new String[] {"login_invalid"};
			}
			ch.name = parts[1];
			return new String[] {"login_valid"};
		}
	}

	private static class LogoutHandler extends CommandHandler {
		{ verb = "logout"; argCount = 0; }

		@Override
		public String[] handle(ConnectionHandler ch, String[] parts) {
			ch.name = null;
			return new String[] {"logout_ack"};
		}
	}

	private static class DisconnectHandler extends CommandHandler {
		{ verb = "disconnect"; argCount = 0; }

		@Override
		public String[] handle(ConnectionHandler ch, String[] parts) {
			ch.interruptCause = InterruptCause.DISCONNECT;
			return new String[] {"disconnect_ack"};
		}
	}

	private static final CommandHandler[] commandHandlers = {
		new LoginHandler(),
		new LogoutHandler(),
		new DisconnectHandler(),
	};
}