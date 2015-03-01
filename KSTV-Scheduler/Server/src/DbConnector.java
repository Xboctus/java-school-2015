import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;

public final class DbConnector {
	private static Connection dbCon;

	public enum ActionStatement {
		TEST("SELECT name FROM Users WHERE name = ?"),
		AUTHENTICATE("SELECT 1 FROM Users WHERE name = ? AND pass = ?"),
		CREATE_USER("INSERT INTO Users (name, pass, timezone, active) values (?, ?, ?, ?)");

		private String statementStr;

		ActionStatement(String statementStr) {
			this.statementStr = statementStr;
		}
	}

	public static PreparedStatement createStatement(ActionStatement type) throws SQLException {
		return dbCon.prepareStatement(type.statementStr);
	}

	public static void init() throws Exception {
		Path conFile = Paths.get(Server.servletContext.getRealPath("/WEB-INF/connection.txt"));
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
	}

	public static void destroy() throws SQLException {
		dbCon.close();
	}
}
