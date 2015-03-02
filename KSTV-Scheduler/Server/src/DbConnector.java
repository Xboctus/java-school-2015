import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;

public final class DbConnector {
	private static Connection dbCon;

	public enum ActionStatement {
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

	public static void init() throws IOException, ClassNotFoundException, SQLException {
		Path conParsFilePath = Paths.get(Server.servletContext.getRealPath("/WEB-INF/connection.txt"));

		String driverClassName;
		String url;
		try (BufferedReader br = Files.newBufferedReader(conParsFilePath, StandardCharsets.UTF_8)) {
			driverClassName = br.readLine();
			url = br.readLine();
		}

		Class.forName(driverClassName);
		dbCon = DriverManager.getConnection("jdbc:" + url);
	}

	public static void destroy() throws SQLException {
		dbCon.close();
	}
}
