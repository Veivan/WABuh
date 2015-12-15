package chatapi;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import chatapi.Constants;

public class SqliteMessageStore implements MessageStoreInterface {
	private String dbfileName;
	private Connection connection;

	public SqliteMessageStore(String number) throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");

		connection = null;
		try {
			dbfileName = System.getProperty("user.dir") + File.separator
					+ Constants.DATA_FOLDER + File.separator + "msgstore-"
					+ number + ".db";
			File f = new File(dbfileName);
			if (!f.exists())
				f.createNewFile();

			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ dbfileName);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement
					.executeUpdate("CREATE TABLE IF NOT EXISTS messages (from String, to String, message String, id String, t String)");
			connection.commit();
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

	@Override
	public void saveMessage(String from, String to, String txt, String id,
			String t) {
		String sql = "INSERT INTO messages (from, to, message, id, t) VALUES (:from, :to, :message, :messageId, :t)";

		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ dbfileName);
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, from);
			statement.setString(2, to);
			statement.setString(3, txt);
			statement.setString(4, id);
			statement.setString(1, t);
			statement.execute();
			connection.commit();
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}
}
