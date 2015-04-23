// TODO: connection pooling

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class DbConnector {
	private static Connection dbCon;

	static PreparedStatement createStatement(String statement) throws SQLException {
		return dbCon.prepareStatement(statement);
	}

	static void init() throws IOException, ClassNotFoundException, SQLException {
		String driverClassName;
		String url;

		Path conParsFilePath = Paths.get(Server.getRealPath("/WEB-INF/connection.txt"));
		try (BufferedReader br = Files.newBufferedReader(conParsFilePath, StandardCharsets.UTF_8)) {
			driverClassName = br.readLine();
			url = br.readLine();
		}

		Class.forName(driverClassName);
		dbCon = DriverManager.getConnection("jdbc:" + url);
	}

	static void fin() throws SQLException {
		try {
			dbCon.close();
		} finally {
			dbCon = null;
		}
	}
}
