package net.floodlightcontroller.core.coap.dataparsers;

import org.openflow.protocol.statistics.OFStatistics;

/**
 * The Parse interface is implemented by all classes that are used to parse the COAP related
 * wireless statistics collected from the COAP APs.
 * 
 * @author "Ashish Patro"
 *
 */
public interface Parser {
	//void process(String rest, int apId);
	
	/**
	 * Process the input statistics.
	 * 
	 * @param currStat
	 * @param apId
	 */
	public void processOFStat(OFStatistics currStat, int apId);
	
	/**
	 * Get reference to the in-memory HashMap maintained for inserting the statistics into a database.
	 * 
	 * @return Data HashMap
	 */
	public Object getStorageHashMap();
	
	/**
	 * Get reference to the in-memory HashMap maintained for real-time data-analytics.
	 * 
	 * @return Data HashMap
	 */
	public Object getInMemoryHashMap();
	
	/**
	 * Commit the data to a backing database from the in-memory HashMap.
	 * 
	 * @param tsLimit
	 */
	public void commit(long tsLimit);
	
	/**
	 * Get the timestamp of the most recent stats update received by the server.
	 * 
	 * @return timestamp value
	 */
	public long getMaxTs();
	
	/**
	 * Remove the stale data entries from the in-memory HashMap. 
	 *  
	 * @param tsLimit - Current timestamp
	 * 
	 * @return number of entries removed. 
	 */
	public int ClearInMemoryData(long tsLimit);
}
