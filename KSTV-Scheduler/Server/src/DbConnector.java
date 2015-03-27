import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.*;

public final class DbConnector {
	// TODO: use connection pool
	private static Connection dbCon;

	public interface StatementType {
		String getStatement();
	}

	public static PreparedStatement createStatement(StatementType type) throws SQLException {
		return dbCon.prepareStatement(type.getStatement());
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
