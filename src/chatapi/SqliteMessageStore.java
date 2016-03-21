package chatapi;

import helper.MessageWA;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import settings.Constants;

public class SqliteMessageStore implements MessageStoreInterface {
	private String dbfileName;
	private Connection connection;

	public SqliteMessageStore(String number) throws ClassNotFoundException {
		construct(number, "");
	}

	public SqliteMessageStore(String number, String customPath)
			throws ClassNotFoundException {
		construct(number, customPath);
	}

	private void construct(String number, String customPath)
			throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");

		connection = null;
		Statement statement = null;
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
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.

			statement
					.executeUpdate("CREATE TABLE IF NOT EXISTS messages (fromN TEXT, toN TEXT, message TEXT, id TEXT, t TEXT)");
			connection.commit();
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			if (connection != null) {
				try {
					connection.rollback();
				} catch (SQLException excep) {
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					System.err.println(e.getMessage());
				}
			}
			try {
				if (connection != null) {
					connection.setAutoCommit(true);
					connection.close();
				}
			} catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	@Override
	public void saveMessage(String from, String to, String txt, String id,
			String t) {
		String sql = "INSERT INTO messages (fromN, toN, message, id, t) VALUES (:from, :to, :message, :messageId, :t)";

		try {
			// create a database connection
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ dbfileName);
			connection.setAutoCommit(false);
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
				if (connection != null) {
					connection.setAutoCommit(true);
					connection.close();
				}
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

	public void setPending(String id, String jid) { // TODO kkk
		/*
		 * $sql = 'UPDATE messages_pending set `pending` = 1, `jid` = :jid where
		 * `id` = :id'; $query = $this->db->prepare($sql); $query->execute( [
		 * ':id' => $id, ':jid' => $jid, ] ); $sql = 'INSERT OR IGNORE into
		 * messages_pending(`id`,`jid`, `pending`) VALUES(:id,:jid,1)'; $query =
		 * $this->db->prepare($sql); $query->execute( [ ':id' => $id, ':jid' =>
		 * $jid, ] );
		 */
	}

	public ArrayList<MessageWA> getPending(String jid) {
		// TODO kkk
		/*
		 * $sql = 'SELECT `id` from messages_pending where `jid` = :jid and
		 * `pending` = 1'; $query = $this->db->prepare($sql); $query->execute( [
		 * ':jid' => $jid, ] ); $pending_ids = []; while ($row =
		 * $query->fetch(PDO::FETCH_ASSOC)) { if ($row != null && $row !==
		 * false) { $pending_ids[] = $row['id']; } } if (count($pending_ids) ==
		 * 0) { return []; } $messages = []; $qMarks = str_repeat('?,',
		 * count($pending_ids) - 1).'?'; $sql =
		 * "SELECT * from messages where `id` IN ($qMarks)"; $query =
		 * $this->db->prepare($sql); $query->execute($pending_ids); while ($row
		 * = $query->fetch(PDO::FETCH_ASSOC)) { if ($row != null && $row !==
		 * false) { $messages[] = $row; } } $sql = 'DELETE FROM messages_pending
		 * where `pending` = 1 and jid = :jid'; $query =
		 * $this->db->prepare($sql); $query->execute([':jid' => $jid]);
		 * 
		 * return $messages;
		 */

		ArrayList<MessageWA> messages = new ArrayList<MessageWA>();
		return messages;

	}
}
