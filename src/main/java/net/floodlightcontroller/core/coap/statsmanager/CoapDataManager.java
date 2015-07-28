package net.floodlightcontroller.core.coap.statsmanager;

import java.util.List;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFApinfoStatisticsReply;
import org.openflow.protocol.statistics.coap.OFBeaconStatisticsReply;
import org.openflow.protocol.statistics.coap.OFClientStatisticsReply;
import org.openflow.protocol.statistics.coap.OFNonWiFiStatisticsReply;
import org.openflow.protocol.statistics.coap.OFPassiveStatisticsReply;
import org.openflow.protocol.statistics.coap.OFStationStatisticsReply;
import org.openflow.protocol.statistics.coap.OFTrafficinfoStatisticsReply;
import org.openflow.protocol.statistics.coap.OFUtilStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.coap.dataparsers.*;

import net.floodlightcontroller.core.coap.experiments.PassiveHopParser;
import net.floodlightcontroller.core.coap.experiments.UtilHopParser;
import net.floodlightcontroller.core.coap.util.CoapConstants;

/**
 * This class constitutes a part of "StatsManager" module of the COAP server.
 * 
 * CoapDataManager manages the statistics collected from the COAP APs. It parses
 * the data returned by COAP APs and maintains them in-memory for COAP related
 * functions. It also provides a function to commit the data to persistent storage.
 * 
 * @author "Ashish Patro"
 *
 */
public class CoapDataManager {
	// Logger.
    protected static Logger log = 
        LoggerFactory.getLogger(CoapDataManager.class);
    
    // Initialize parsers for processing different types of statistics collected 
    // from the routers.
	private static Parser beaconParser = new BeaconStatsParser();

	private static Parser nonwifiParser = new NonWiFiStatsParser();
	private static Parser passiveParser = new PassiveStatsParser();
	private static Parser stationStatsParser = new StationStatsParser();
	private static Parser utilParser = new AirtimeUtilParser();
	
	// Debug stats related parsers.
	private static Parser passiveHopParser = new PassiveHopParser();
	private static TrafficInfoParser trafficInfoParser = new TrafficInfoParser();
	private static UtilHopParser utilHopParser = new UtilHopParser();
	
	private static long maxDataCommitTs = 0;
	
	/**
	 * Process the AP information statistics.
	 * TODO: 0501 Bug for is5GhzSupported value.
	 * 
	 * @param apInfoStats
	 * @param apId
	 */
	public static void processApinfoStats(List<OFStatistics> apInfoStats, int apId) {
    	if (apInfoStats != null) {
    		log.warn("Got " + apInfoStats.size() + " NICs for apId " + apId);

    		// Maintain the neighborhood map.
    		NeighborhoodMapManager.updateApinformation(apId, apInfoStats);

    		for (int i = 0; i < apInfoStats.size(); i++) {
    			log.info("Got " + i + " " + ((OFApinfoStatisticsReply) apInfoStats.get(i)).toString());
    		}
    	}
    }

    /**
     * Process the airtime utilization statistics.
     * 
     * @param airtimeUtilStats
     * @param apId
     */
    public static void processAirtimeUtilStats(List<OFStatistics> airtimeUtilStats, int apId) {
    	if (airtimeUtilStats != null) {
    		for (int i = 0; i < airtimeUtilStats.size(); i++) {
    			log.info("Got " + i + " " + ((OFUtilStatisticsReply) airtimeUtilStats.get(i)).toString());
    			utilParser.processOFStat(airtimeUtilStats.get(i), apId);

    			// Debug related
    			processDataString(((OFUtilStatisticsReply) airtimeUtilStats.get(i)).getUtilhopStatsString().trim(), apId, "getUtilhopStatsString " + i);
    		}
    	}
    }

    /**
     * Process the packet transmission statistics of local clients.
     * 
     * @param stationStats
     * @param apId
     */
    public static void processStationStats(List<OFStatistics> stationStats, int apId) {
    	if (stationStats != null) {
    		log.warn("Got " + stationStats.size() + " station instances for apId " + apId);

    		for (int i = 0; i < stationStats.size(); i++) {
    			log.info("Got " + i + " " + ((OFStationStatisticsReply) stationStats.get(i)).toString());

    			stationStatsParser.processOFStat(stationStats.get(i), apId);
    		}
    	} else {
    		log.warn("stationStats is null for apId " + apId);
    	}
    }
    
	/**
	 * Process information about the neighboring APs observed by the current AP.
     * 
     * @param beaconStats
     * @param apId
     */
    public static void processBeaconStats(List<OFStatistics> beaconStats, int apId) {
    	if (beaconStats != null) {
    		log.warn("Got " + beaconStats.size() + " beacons for apId " + apId);

    		// Maintain the neighborhood map.
    		NeighborhoodMapManager.updateNeighboringAPs(apId, beaconStats);

    		for (int i = 0; i < beaconStats.size(); i++) {
    			log.info("Got " + i + " " + ((OFBeaconStatisticsReply) beaconStats.get(i)).toString());
    			beaconParser.processOFStat(beaconStats.get(i), apId);
    		}
    	} else {
    		log.warn("beaconStats is null for apId " + apId);
    	}
    }
    
	/** Process the non-WiFi activity observed the AP.
     *
     * @param nonWiFiStats
     * @param apId
     */
    public static void processNonwifiStats(List<OFStatistics> nonWiFiStats, int apId) {
    	if (nonWiFiStats != null) {
    		log.warn("Got " + nonWiFiStats.size() + " nonWiFi instances for apId " + apId);

    		for (int i = 0; i < nonWiFiStats.size(); i++) {
    			log.info("Got " + i + " " + ((OFNonWiFiStatisticsReply) nonWiFiStats.get(i)).toString());
    			nonwifiParser.processOFStat(nonWiFiStats.get(i), apId);
    		}
    	} else {
    		log.warn("nonWiFiStats is null for apId " + apId);
    	}
    }
	
    /**
     * Process aggregate statistics about the neighboring WiFi activity observed by the AP.
     * 
     * @param passiveStats
     * @param apId
     */
    public static void processPassiveStats(List<OFStatistics> passiveStats, int apId) {
    	if (passiveStats != null) {
    		log.warn("Got " + passiveStats.size() + " passive instances for apId " + apId);

    		for (int i = 0; i < passiveStats.size(); i++) {
    			OFPassiveStatisticsReply currStat = (OFPassiveStatisticsReply) passiveStats.get(i);
    			log.info("Got " + i + " " + currStat.toString());

    			if (currStat.getType() == 0) {
    				passiveParser.processOFStat(currStat, apId);
    			} else {
    				passiveHopParser.processOFStat(currStat, apId);
    			}
    		}
    	} else {
    		log.warn("passiveStats is null for apId " + apId);
    	}
    }
    
	/**
	 * Process additional information about the clients (e.g., hostname, device Id etc.)
     * TODO - Insert the data into a DB.
     * 
     * @param clientStats
     * @param apId
     */
    public static void processClientStats(List<OFStatistics> clientStats, int apId) {
    	if (clientStats != null) {
    		log.warn("Got " + clientStats.size() + " client instances for apId " + apId);

    		for (int i = 0; i < clientStats.size(); i++) {
    			log.info("Got " + i + " " + ((OFClientStatisticsReply) clientStats.get(i)).toString());

    			//process_string(((OFClientStatisticsReply) clientStats.get(i)).getClientDevString().trim(), apId, "getClientDevString " + i);
    		}
    	} else {
    		log.warn("clientStats is null for apId " + apId);
    	}
    }
    
    /**
     * Process the traffic related statistics.
     * 
     * @param trafficinfoStats
     * @param apId
     */
    public static void processTrafficinfoStats(List<OFStatistics> trafficinfoStats, int apId) {
    	if (trafficinfoStats != null) {
    		for (int i = 0; i < trafficinfoStats.size(); i++) {
    			processDataString(((OFTrafficinfoStatisticsReply) trafficinfoStats.get(i)).getTrafficInfoStatsString().trim(), 
    					apId, "trafficinfoStats " + i);
    		}
    	} else {
    		log.warn("trafficinfoStats is null for apId " + apId);
    	}
    }
	  
    /**
     * Process the input stats data string. Some debug and temporary statistics and collected
     * as a formatted string. This function is used to process those statistics after they
     * are received from the COAP APs.
     * 
     * 
     * @param dataString
     * @param apId
     * @param debugType
     * @return Success/Failure status
     */
    private static int processDataString(String dataString, int apId, String debugType) {

		try {
			String[] terms = dataString.split(";");
			
			if (terms.length == 1) {
				log.info("skipping stats type: " + terms[0] + " for ap: " + apId + " debugType: " + debugType);
				return -1;
			}
			
			String rest = "";
			for (int i = 1; i < terms.length - 1; ++i) {
				rest += terms[i];
				rest += ";";
			}
			
			rest += terms[terms.length - 1];
	
			log.info(terms[0] + " : stats for ap " + apId + " rest: " + rest);
			
			if (terms[0].equals(CoapConstants.TRAFFICINFO_MSG)) {
				log.info("stats for ap " + apId + " type: " + terms[0] + " rest:" + rest);
				trafficInfoParser.process(rest, apId);
			} else if (terms[0].equals(CoapConstants.UTILHOP_MSG)) {
				utilHopParser.process(rest, apId);
			}
			
		} catch (Exception ex) {
			log.error("process_string: " + ex.getMessage());
			
			ex.printStackTrace();
			return -1;
		}
		return 0;
	}

    /**
     * Clears the in-memory statistics. It uses a sliding window of "currTs" seconds
     * that clears the in-memory data older than the sliding window value.
     * 
     * @param currTs
     * @return total removed entries. 
     */
    public static int clearInMemoryData(long currTs) {  // sliding window implementation

		int cnt = 0;

		// Clear the util hop stats.
		cnt += utilHopParser.ClearInMemoryData(currTs);

		// Clear the util stats.
		cnt += utilParser.ClearInMemoryData(currTs);

		// Clear the station stats.
		cnt += stationStatsParser.ClearInMemoryData(currTs);

		// Clear the higher layer stats.
		cnt += trafficInfoParser.ClearInMemoryData(currTs);

		// TODO: Test.
		cnt += passiveParser.ClearInMemoryData(currTs);

		// TODO: Test.
		cnt += passiveHopParser.ClearInMemoryData(currTs);

		// TODO: Test.
		cnt += nonwifiParser.ClearInMemoryData(currTs);

		//synchronized
		return cnt;
	}
    
    /**
     * Store the collected statistics to persistent storage. The intervalSec input is optionally
     * used to specify the most recent time window for selecting the amount of recent data to
     * be stored. 
     * 
     * @param intervalSec
     * @param showLogs
     */
    public static void commitPersistentData(long intervalSec, boolean showLogs) {
    	long ts = System.currentTimeMillis();
    			
    	beaconParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Beacon stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		ts = System.currentTimeMillis();
		trafficInfoParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Higher stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		ts = System.currentTimeMillis();
		passiveParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Passive stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		//ClientWorker.pie_parser.commit();

		ts = System.currentTimeMillis();
		stationStatsParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Station stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		ts = System.currentTimeMillis();
		utilParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Util stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		ts = System.currentTimeMillis();
		utilHopParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Util hop stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		ts = System.currentTimeMillis();
		passiveHopParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Passive hop stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		ts = System.currentTimeMillis();
		nonwifiParser.commit(intervalSec);

		if (showLogs) {
			System.out.println("Nonwifi stat time: " + (System.currentTimeMillis() - ts) + "ms");
		}

		maxDataCommitTs = Math.max(trafficInfoParser.getMaxTs(), maxDataCommitTs);
		maxDataCommitTs = Math.max(utilHopParser.getMaxTs(), maxDataCommitTs);
		maxDataCommitTs = Math.max(utilParser.getMaxTs(), maxDataCommitTs);
		maxDataCommitTs = Math.max(stationStatsParser.getMaxTs(), maxDataCommitTs);
		maxDataCommitTs = Math.max(passiveParser.getMaxTs(), maxDataCommitTs);
		maxDataCommitTs = Math.max(passiveHopParser.getMaxTs(), maxDataCommitTs);
		maxDataCommitTs = Math.max(nonwifiParser.getMaxTs(), maxDataCommitTs);

    }
    
	public static Parser getBeaconParser() {
		return beaconParser;
	}

	public static Parser getNonwifiParser() {
		return nonwifiParser;
	}

	public static Parser getPassiveParser() {
		return passiveParser;
	}

	public static Parser getStationStatsParser() {
		return stationStatsParser;
	}

	public static Parser getUtilParser() {
		return utilParser;
	}

	public static Parser getPassiveHopParser() {
		return passiveHopParser;
	}

	public static TrafficInfoParser getTrafficInfoParser() {
		return trafficInfoParser;
	}

	public static UtilHopParser getUtilHopParser() {
		return utilHopParser;
	}

	public static long getMaxDataCommitTs() {
		return maxDataCommitTs;
	}
}
