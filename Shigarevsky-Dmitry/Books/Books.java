import java.util.*;
import java.nio.file.*;
import java.sql.*;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Books {
	private static final String bookDirName = "./books";
	private static final Path bookPath = Paths.get(bookDirName);
	
	private static final String dbServerName = "localhost";
	private static final String dbUserId = "root";
	private static final String dbUserPassword = "qwerty";
	
	private static BookParser bookParser;
	private static Connection connection;
	private static WatchService watchService;

	private static void printUsageAndQuit() {
		System.err.println("Usage: java -Dalgorithm=<algo> Books , where <algo>");
		System.err.println("dom - DOM (Document Object Model, tree based)");
		System.err.println("sax - SAX (Simple API for XML, event based)");
		System.exit(-1);
	}
	
	private static void initialize() {
		String algo = System.getProperties().getProperty("algorithm");
		if (algo == null) {
			printUsageAndQuit();
		} else if (algo.equals("dom")) {
			bookParser = new DomBookParser();
		} else if (algo.equals("sax")) {
			bookParser = new SaxBookParser();
		} else {
			printUsageAndQuit();
		}
	}
	
	private static void establishConnection() {
		MysqlDataSource ds = new MysqlDataSource();
		ds.setServerName(dbServerName);
		ds.setUser(dbUserId);
		ds.setPassword(dbUserPassword);
		
		try {
			connection = ds.getConnection();
		} catch (SQLException e) {
			System.err.format("Connection to database failed: %s\n", e.getMessage());
			System.exit(-1);
		}
	}
	
	private static void registerWatchService() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			bookPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
		} catch (Exception e) {
			System.err.format("Creating or registering watch service failed: ", e.getMessage());
			System.exit(-1);
		}
	}
	
	private static WatchKey takeWatchKey() {
		try {
			return watchService.take();
		} catch (InterruptedException e) {
			System.err.format("Waiting for watch key was interrupted: ", e.getMessage());
			System.exit(-1);
			return null;
		}
	}
	
	private static void pushBook(Book book) {
		try {
			PreparedStatement statement = connection.prepareStatement(
				"REPLACE INTO books.books (id, isbn, author, title, genre, description, publish_date, price) " + 
				"VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
			);
			statement.setString(1, book.id);
			statement.setString(2, book.isbn != null ? book.isbn : "");
			statement.setString(3, book.author);
			statement.setString(4, book.title);
			statement.setString(5, book.genre);
			statement.setString(6, book.description);
			statement.setDate(7, java.sql.Date.valueOf(book.publishDate));
			statement.setFloat(8, book.price);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			System.err.format("Pushing book to database failed: %s", e.getMessage());
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		initialize();
		establishConnection();
		registerWatchService();
		
		while (true) {
			WatchKey watchKey = takeWatchKey();
			
			for (WatchEvent<?> event_: watchKey.pollEvents()) {
				@SuppressWarnings("unchecked")
				WatchEvent<Path> event = (WatchEvent<Path>)event_;

				WatchEvent.Kind<Path> kind = event.kind();
				Path name = event.context();
				Path fullName = bookPath.resolve(name);
				System.out.format("%s: %s\n", kind, fullName);
				
				if (name.toString().toLowerCase().endsWith(".xml")) {
					if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
						ArrayList<Book> books = bookParser.parse(fullName.toString());
						if (books == null) {
							System.err.format("Parsing of '%s' failed.\n", fullName);
							System.exit(-1);
						}
						for (Book book: books) {
							pushBook(book);
						}
					}
				}
			}
			
			if (!watchKey.reset()) {
				System.err.println("Watch key reset failed.");
				break;
			}
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			System.err.format("Closing connection to database failed: %s\n", e.getMessage());
			System.exit(-1);
		}
	}
}
