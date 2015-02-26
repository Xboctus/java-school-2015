import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;
import javax.servlet.*;

public final class DbConnector {
	private static Connection dbCon;

	public enum ActionStatement {
		TEST("SELECT name FROM Users WHERE name = ?"),
		AUTHENTICATE("SELECT 1 FROM Users WHERE name = ? AND pass = ?"),
		CREATE_USER("INSERT INTO Users (name, pass, timezone, active) values (?, ?, ?, ?)");

		private String statementStr;
		public PreparedStatement statement;

		ActionStatement(String staement) {
			this.statementStr = staement;
		}

		public void prepare() throws SQLException {
			statement = dbCon.prepareStatement(statementStr);
		}
	}

	public static void init(ServletContext sc) throws Exception {
		Path conFile = Paths.get(sc.getRealPath("/WEB-INF/connection.txt"));
		String driverStr;
		String conStr;
		try (BufferedReader br = Files.newBufferedReader(conFile, StandardCharsets.UTF_8)) {
			driverStr = br.readLine();
			conStr = br.readLine();
		} catch (IOException e) {
			throw e;
		}

		Class.forName(driverStr);
		dbCon = DriverManager.getConnection("jdbc:" + conStr);

		for (ActionStatement ps: ActionStatement.values()) {
			try {
				ps.prepare();
			} catch (SQLException e) {
				dbCon.close(); // FIXME: if throws, e is lost
				throw e;
			}
		}
	}

	public static void destroy() throws SQLException {
		try {
			for (ActionStatement ps: ActionStatement.values()) {
				ps.statement.close(); // FIXME: if throws, subsequent statements are not closed
			}
		} finally {
			dbCon.close(); // FIXME: if throws, previously thrown exception is lost
		}
	}
}
