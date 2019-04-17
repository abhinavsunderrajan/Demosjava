package networkutils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The class responsible for handling database operations
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class DatabaseAccess {

	private Connection connect = null;
	private PreparedStatement blockExecute;
	private int counter = 0;
	private int batchSize = 100;
	private Properties connectionProperties;
	private static final Logger LOGGER = Logger.getLogger(DatabaseAccess.class);

	/**
	 * default constructor
	 */
	public DatabaseAccess() {

	}

	/**
	 * Create and open a database connection using a properties file. The
	 * assumed driver here is PostgreSQL
	 * 
	 * @param connectionProperties
	 */
	public DatabaseAccess(final Properties connectionProperties) {
		connectionProperties.put("database.driver", "org.postgresql.Driver");
		openDBConnection(connectionProperties);
	}

	/**
	 * Create and open a database connection using a properties file and a
	 * different driver other than postgres.
	 * 
	 * @param connectionProperties
	 * @param driver
	 */
	public DatabaseAccess(final Properties connectionProperties, String driver) {
		connectionProperties.put("database.driver", driver);
		openDBConnection(connectionProperties);
	}

	public void initializeDatabaseAccess(final Properties connectionProperties) {
		openDBConnection(connectionProperties);
	}

	/**
	 * Create and open a database connection
	 * 
	 * @param url
	 *            the database URL
	 * @param userName
	 *            the user name
	 * @param password
	 *            the password.
	 * @param driver
	 *            the database driver name
	 */
	public DatabaseAccess(String url, String userName, String password, String driver) {
		openDBConnection(url, userName, password, driver);
	}

	/**
	 * 
	 * @param connectionProperties
	 * @return
	 */
	private void openDBConnection(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
		String url = connectionProperties.getProperty("database.url");
		String dbName = connectionProperties.getProperty("database.name");
		String userName = connectionProperties.getProperty("database.username");
		String password = connectionProperties.getProperty("database.password");
		String driver = connectionProperties.getProperty("database.driver");
		try {
			Class.forName(driver).newInstance();
			connect = (Connection) DriverManager.getConnection(url + dbName, userName, password);

		} catch (Exception e) {
			LOGGER.error("Unable to connect to database. Please check the settings", e);
		}

	}

	/**
	 * Open a database connection that is user specific. Used to store the
	 * results of the simulation to the local database of the submittee.
	 * 
	 * @param url
	 * @param userName
	 * @param password
	 * @param driver
	 */
	private void openDBConnection(String url, String userName, String password, String driver) {
		try {
			Class.forName(driver).newInstance();

			connect = (Connection) DriverManager.getConnection(url, userName, password);

		} catch (Exception e) {
			LOGGER.error("Unable to connect to database. Please check the settings", e);
		}

	}

	/**
	 * Return the result set for the SELECT query.
	 * 
	 * @param queryString
	 * @return
	 */
	public ResultSet retrieveQueryResult(String queryString) {

		ResultSet resultSet = null;
		try {
			PreparedStatement preparedStatement = (PreparedStatement) connect.prepareStatement(queryString);
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return resultSet;
	}

	/**
	 * Return the result of a query as a query as a cursor. Suitable for large
	 * tables.
	 * 
	 * @param queryString
	 * @param cursorsize
	 * @return
	 */
	public ResultSet retrieveQueryResult(String queryString, int cursorsize) {

		ResultSet resultSet = null;
		try {
			connect.setAutoCommit(false);
			PreparedStatement preparedStatement = (PreparedStatement) connect.prepareStatement(queryString);
			preparedStatement.setFetchSize(cursorsize);
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return resultSet;
	}

	/**
	 * Call for DDL statements i.e. DELETE, UPDATE and INSERT
	 * 
	 * @param queryString
	 */
	public void executeUpdate(String queryString) {
		try {
			PreparedStatement preparedStatement = (PreparedStatement) connect.prepareStatement(queryString);
			preparedStatement.execute();

		} catch (SQLException e) {
			// System.out.println(queryString);
			LOGGER.error("Error while executing the DDL statement", e);
			openDBConnection(connectionProperties);
		}
	}

	/**
	 * Set up a prepared statement for block execute
	 * 
	 * @param batchSize
	 *            the size of batch for performing batch DDL/DML executes.
	 * @param sql
	 *            the statement to be repeatedly executed.
	 * @throws SQLException
	 */
	public void setBlockExecutePS(String sql, int batchSize) throws SQLException {
		blockExecute = connect.prepareStatement(sql);
		this.batchSize = batchSize;
	}

	/**
	 * Execute the block insert.
	 * 
	 * @param vals
	 * @throws SQLException
	 */
	public void executeBlockUpdate(Object... vals) throws SQLException {

		for (int i = 0; i < vals.length; i++) {
			blockExecute.setObject(i + 1, vals[i]);
		}

		blockExecute.addBatch();
		if ((counter + 1) % batchSize == 0) {
			blockExecute.executeBatch();
			blockExecute.clearBatch();
		}
		counter++;

	}

	/**
	 * Close the database connection.
	 * 
	 * @throws SQLException
	 */

	public void closeConnection() throws SQLException {
		connect.close();
	}

	/**
	 * @return the blockExecute
	 */
	public PreparedStatement getBlockExecute() {
		return blockExecute;
	}

	public Connection getConnect() {
		return connect;
	}

}
