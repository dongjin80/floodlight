package net.floodlightcontroller.core.coap.statsmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;

/**
 * This class constitutes a part of "StatsManager" module of the COAP server and 
 * periodically commits the collected statistics to a persistent storage.
 * 
 * @author "Ashish Patro"
 *
 */
public class DatabaseCommitter implements Runnable {
	public static Connection conn;
	protected static Logger log = 
			LoggerFactory.getLogger(DatabaseCommitter.class);

	/**
	 * Instantiates a connection to the MySQL backend.
	 */
	public DatabaseCommitter() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		// TODO 0214: Using this different table for experiments.
		//String connection = "jdbc:mysql://localhost:3306/wahdata_expts?user=root&password=1234";
		//String connection = "jdbc:mysql://10.1.0.1:3306/wahdata_expts?user=root&password=699.tmp";
		
		String connection = "jdbc:mysql://localhost:3306/coap_db?user=root&password=699.tmp";
		
		//String connection = "jdbc:mysql://localhost:3306/wahdata_expts?user=root&password=699.tmp";

		//String connection = "jdbc:mysql://128.105.22.232:3306/wahdata?user=ashish&password=wingswifi";
		try {
			//String connection = "jdbc:mysql://localhost:3306/wahdata?user=root&password=699.tmp";
			//String connection = "jdbc:mysql://localhost:3306/wahdata?user=root&password=lily1986";

			/*
      		String connection = "jdbc:mysql://" + CoapConstants.SERVER_HOSTNAME + 
          		":" + CoapConstants.SERVER_PORT + "/" + CoapConstants.DB_NAME +
          		"?user=" + CoapConstants.DB_USER_NAME + "&password=" + CoapConstants.DB_PASSWORD;
			 */

			conn = DriverManager.getConnection(connection);
		} catch (SQLException e) {
			System.err.println("Exception for connection: " + connection + " Error: " + e.toString());
			System.exit(1);
		}
	}

	/**
	 * Execute the input query.
	 * 
	 * @param query
	 * @return Success/Failure status
	 */
	public static int executeQuery(String query) {
		try {
			Statement stat = conn.createStatement();
			int ret = stat.executeUpdate(query);
			
			if (ret < 0) {
				log.error(String.format("query %s failed", query));
				return -1;
			}
		} catch (SQLException e) {
			log.error("DatabaseCommitter: query execute failed: " + e.getMessage());

			System.err.println("query execute failed " + e.toString());
			return -1;
		}

		return 0;
	}

	/**
	 * Execute the input query.
	 * 
	 * @param queryFormat
	 * @param params
	 * @return Success/Failure status
	 */
	public static int executeQuery(String queryFormat, 
			ArrayList<ArrayList<Object>> params) {
		long ts = System.currentTimeMillis();
		PreparedStatement statement;
		
		try {
			conn.setAutoCommit(false);
			statement = conn.prepareStatement(queryFormat);
		} catch (SQLException e) {
			log.error("DatabaseCommitter: ExecuteQuery1 " + e.getMessage());

			e.printStackTrace();
			return -1;
		}

		for (ArrayList<Object> objectArray: params) {
			try {
				int i = 0;
				for (Object obj: objectArray) {
					i += 1;
					if (obj instanceof Integer) {
						statement.setInt(i, (Integer)obj);
					} else if (obj instanceof Float) {
						statement.setFloat(i, (Float)obj);
					} else if (obj instanceof Double) {
						statement.setDouble(i, (Double)obj);
					} else if (obj instanceof String) {
						statement.setString(i, (String)obj);
					} else if (obj instanceof Long) {
						statement.setLong(i, (Long)obj);
					}
				}

				statement.addBatch();
			} catch (SQLException ex) {
				log.error("DatabaseCommitter: ExecuteQuery2 " + ex.getMessage());

				ex.printStackTrace();
				return -1;
			}
		}

		if (params.size() > 0) {
			try {
				statement.executeBatch();
				conn.commit();
			} catch (SQLException e) {
				e.printStackTrace();
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}

				return -1;
			}
		}

		try {
			conn.setAutoCommit(true);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}

		long ts2 = System.currentTimeMillis() - ts;
		System.out.println("Entries_prepared: " + params.size() + " Time: " + ts2 / 1000);
		return 0;
	}

	/**
	 * Executes the input list of queries as a batch and return the success status.
	 * 
	 * @param queries
	 * @return Success/Failure
	 */
	public static int executeQuery(ArrayList<String> queries) {
		try {
			long ts = System.currentTimeMillis();

			Statement stat = conn.createStatement();
			for (String query: queries) {
				stat.addBatch(query);
			}

			int[] rets = stat.executeBatch();
			for (int i = 0; i < rets.length; ++i) {
				if (rets[i] < 0) {
					String query = queries.get(i);
					log.error(String.format("query %s failed", query));
				}
			}

			long ts2 = System.currentTimeMillis()  - ts;

			System.out.println("Entries: " + queries.size() + " Time: " + ts2 / 1000);
		} catch (SQLException e) {
			log.error("DatabaseCommitter: ExecuteQuery3 " + e.getMessage());

			System.err.println("query execute failed " + e.toString());
			return -1;
		}

		return 0;
	}

	/**
	 * Execute the input query (used for select operation) and return the result set.
	 * 
	 * @param query
	 * @return query results
	 */
	public static ResultSet executeSelect(String query) {
		Statement stat;
		try {
			stat = conn.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
			return null;
		}

		ResultSet resultSet = null;
		try {
			resultSet = stat.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return resultSet;
	}

	
	/**
	 * Run the continuous loop to commit the in-memory data to the MySQL backend.
	 *  
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			System.out.println("commiting to db ");
			
			try {
				System.out.println("Sleeping for: " + CoapConstants.DB_COMMITTER_SLEEP_INTERVAL_MS + "ms");
				Thread.sleep(CoapConstants.DB_COMMITTER_SLEEP_INTERVAL_MS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// ClientWorker.beacon_parser.commit();
			long ts = System.currentTimeMillis();
			long intervalSec = ts / 1000 - CoapConstants.DB_COMMITTER_DATA_TIMEWINDOW;
			
			boolean showLogs = false;

			try {
				CoapDataManager.commitPersistentData(intervalSec, showLogs);
			} catch (ConcurrentModificationException e) {
				log.error("DatabaseCommitter: run " + e.getMessage());

				e.printStackTrace();
				continue;
			} catch (Exception e) {
				log.error("DatabaseCommitter: exception in DB commit loop " + e.getMessage());

				e.printStackTrace();
				continue;
			}
			
			/*PolicyEngine.lock.lock();
			PolicyEngine.toRead = true;
			PolicyEngine.lock.unlock();*/
		}
	}
}
