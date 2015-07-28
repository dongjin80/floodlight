package net.floodlightcontroller.core.coap.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Miscellaneous utility functions used by the COAP server.
 * 
 * @author "Ashish Patro"
 *
 */
public class CoapUtils {

	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";	

	/**
	 * Return the current time in String format.
	 * @return time string
	 */
	public static String getCurrentTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	/**
	 * For debug testing - Return an AP identifier using its IP.
	 * @param ipString
	 * @return AP ID
	 */
	public static int getTestApIdFromRemoteIp(String ipString) {
		return Integer.parseInt(ipString.split(":")[0].split("\\.")[3]) + 1000;
	}
	
	// 
	/**
	 * Extract and return the IP address from the socket string (Example: /10.1.0.158:42350).
	 * 
	 * @param socketString
	 * @return IP string
	 */
	public static String getIpSockAddrString(String socketString) {
		return socketString.split("/")[1].split(":")[0];
	}

	/**
	 * Returns the 32bit dotted format of the provided long ip.
	 *
	 * @param ip the long ip
	 * @return the 32bit dotted format of <code>ip</code>
	 * @throws IllegalArgumentException if <code>ip</code> is invalid
	 */
	public static String getIpString(long ip) {
		// if ip is bigger than 255.255.255.255 or smaller than 0.0.0.0
		if (ip > 4294967295l || ip < 0) {
			throw new IllegalArgumentException("invalid ip");
		}
		StringBuilder ipAddress = new StringBuilder();
		for (int i = 3; i >= 0; i--) {
			int shift = i * 8;
			ipAddress.append((ip & (0xff << shift)) >> shift);
			if (i > 0) {
				ipAddress.append(".");
			}
		}
		return ipAddress.toString();
	}
	
	/**
	 * Return the WiFi channel number from the actual frequency value.
	 * @param frequency
	 * @return WiFi channel
	 */
	public static int getWiFiChannelFromFreq(int frequency) {

		if (frequency >= 2412 && frequency <= 2472) {
			return (frequency - 2407) / 5;
		} else if (frequency == 2484) {
			return 14;
		} else if (frequency >= 5170 && frequency <= 5825) {
			return 34 + (frequency - 5170) / 5;
		}
		return -1;
	}

	/**
	 * Return the frequency value from the WiFi channel.
	 * 
	 * @param channel
	 * @return WiFi frequency
	 */
	public static int getWiFiFrequencyFromChannel(int channel) {
		
		if (channel >= 1 && channel <= 13) {
			return channel * 5 + 2407;
		} else if (channel >= 34 && channel <= 165) {
			return (channel - 34) * 5 + 5170;
		}

		return -1;
	}

	/*
	public static int getApIdFromFileOrDb(String addr, String filepath, boolean useDb) {
		try {
			if (useDb) {
				return getApIdDb(addr);
			} else {
				return getApIdFile(addr, filepath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}
	
	public static int getApIdFile(String addr, String filepath) {
		try {
			FileInputStream input = new FileInputStream(filepath);
			DataInputStream in = new DataInputStream(input);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String str;
			while ((str = br.readLine()) != null) {
				String[] terms = str.split(" ");

				//System.out.println(terms[0] + " " + addr);
				if (terms[0].equals(addr)) {
					return Integer.parseInt(terms[1]);
				}
			}
		} catch (FileNotFoundException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		}

		return -1;
	}

	public static int getApIdDb(String addrx) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		int ap_id = -1;
		Connection conn = null;

		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + CoapConstants.SERVER_HOSTNAME + 
					":" + CoapConstants.SERVER_PORT + "/" + CoapConstants.CONFIG_DB_NAME +
					"?user=" + CoapConstants.DB_USER_NAME + "&password=" + CoapConstants.DB_PASSWORD);

			if (conn == null) {
				return ap_id;
			}

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery( "SELECT routerid FROM " + CoapConstants.NET_INFO_TABLE + 
					" WHERE mac = '" + addrx.toUpperCase().replace(':', '-') + "'" );
			try {
				while ( rs.next() ) {
					// Return the first known valid AP ID
					ap_id = rs.getInt(1);
					break;
				}

				rs.close();

			} catch (Exception exquery) {
				exquery.printStackTrace();
			}

			try {
				stmt.close();
			} catch (SQLException exstmt) {
				exstmt.printStackTrace();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException exn) { exn.printStackTrace();}
		}

		return ap_id;
	}

	public static String getAddressFromApDb(int ap_id) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		String address = null;
		Connection conn = null;

		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + CoapConstants.SERVER_HOSTNAME + 
					":" + CoapConstants.SERVER_PORT + "/" + CoapConstants.CONFIG_DB_NAME +
					"?user=" + CoapConstants.DB_USER_NAME + "&password=" + CoapConstants.DB_PASSWORD);

			if (conn == null) {
				return address;
			}

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery( "SELECT ipaddr FROM " + CoapConstants.ROUTER_INFO_TABLE + 
					" WHERE routerid = " + ap_id);
			try {
				while ( rs.next() ) {

					// Return the first known valid AP ID
					address = rs.getString(1);
					break;
				}

				rs.close();

			} catch (Exception exquery) {
				exquery.printStackTrace();
			}

			try {
				stmt.close();
			} catch (SQLException exstmt) {
				exstmt.printStackTrace();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException exn) { exn.printStackTrace();}
		}

		return address;
	}

	public static boolean isAutomaticAllowed(int ap_id, int net_id) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}

		boolean isAutoAllowed = false;
		Connection conn = null;

		try {
			conn = DriverManager.getConnection("jdbc:mysql://" + CoapConstants.SERVER_HOSTNAME + 
					":" + CoapConstants.SERVER_PORT + "/" + CoapConstants.CONFIG_DB_NAME +
					"?user=" + CoapConstants.DB_USER_NAME + "&password=" + CoapConstants.DB_PASSWORD);

			// Default to false.
			if (conn == null) {
				return isAutoAllowed;
			}

			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery( "SELECT manual FROM " + CoapConstants.WIRELESS_TABLE + 
					" WHERE routerid = " + ap_id + " and netid = " + net_id);
			try {
				while ( rs.next() ) {
					// return whether automatic is allowed.
					isAutoAllowed = (rs.getInt(0) == 0);
					break;
				}

				rs.close();

			} catch (Exception exquery) {
				exquery.printStackTrace();
			}

			try {
				stmt.close();
			} catch (SQLException exstmt) {
				exstmt.printStackTrace();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException exn) { exn.printStackTrace();}
		}

		return isAutoAllowed;
	}
	*/
}
