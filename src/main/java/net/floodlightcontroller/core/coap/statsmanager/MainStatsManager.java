package net.floodlightcontroller.core.coap.statsmanager;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;

import net.floodlightcontroller.core.coap.util.CoapConstants;
import net.floodlightcontroller.core.coap.util.CoapQueryUtils;
import net.floodlightcontroller.core.coap.util.CoapUtils;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class constitutes a part of "StatsManager" module of the COAP server.
 * 
 * It polls and collects COAP related related information from COAP APs. 
 * The following statistics are colelcted from the COAP APs by this class.
 * - AP information (OFStatisticsType.APINFO).
 * - Associated clients' information (OFStatisticsType.CLIENT).
 * - Aggregate per-client MAC layer statistics OFStatisticsType.STATION).
 * - Airtime utilization statistics (OFStatisticsType.UTIL).
 * - Traffic activity related statistics (OFStatisticsType.TRAFFICINFO).
 * - Passive MAC layer statistics about neighboring WiFi activity (OFStatisticsType.PASSIVE).
 * - Non-WiFi device activity information provided by Airshark module at the APs (OFStatisticsType.NONWIFI). 
 * 
 * @author Ashish Patro
 */
public class MainStatsManager implements Runnable {
	
	// Logger.
    protected static Logger log = 
        LoggerFactory.getLogger(MainStatsManager.class);
    
    /**
     * Store a mapping between the COAP specific AP ID and the OpenFlow datapath ID. 
     */
    private static HashMap<Integer, Long> apIdToDpid = new HashMap<Integer, Long>();
	
    /**
     * Reference to the IFloodlightProviderService instance.
     */
    private IFloodlightProviderService floodlightProvider;
    
    public MainStatsManager(IFloodlightProviderService floodlightProvider) {
    	this.floodlightProvider = floodlightProvider;
    }
    
    /**
     * @param apId
     * @return OpenFlow datapath ID.
     */
    public static long GetDpIdFromAP(int apId) {
    	if (!apIdToDpid.containsKey(apId)) {
    		return -1;
    	}
    	
    	return apIdToDpid.get(apId);
    }
    
	@Override
	public void run() {
		
		while (true) {
			try {
				Thread.sleep(CoapConstants.DATA_POLL_FREQUENCY_MSEC);

				log.info("MainStatsManager: Sending data poll commands to APs");
				
			    Long[] switchDpids = floodlightProvider.getAllSwitchMap().keySet().toArray(new Long[0]);
			    List<StatsQueryThread> activeThreads = new ArrayList<StatsQueryThread>(switchDpids.length);
			    List<StatsQueryThread> pendingRemovalThreads = new ArrayList<StatsQueryThread>();
			    
			    StatsQueryThread t;
			    
			    apIdToDpid = new HashMap<Integer, Long>();
			    
			    for (Long l : switchDpids) {
			        
			        IOFSwitch sw = floodlightProvider.getAllSwitchMap().get(l);
			        int apId = CoapUtils.getTestApIdFromRemoteIp(sw.getInetAddress().toString().toString());
			    
			        apIdToDpid.put(apId, l);
			        log.info("Found " + sw.getInetAddress() + " switchDpid: " + l);
		        	
		        	t = new StatsQueryThread(l, apId, 0); /// CoapConstants.POLL_ALL_COAP_STATS);
		        	t.addPollStat(OFStatisticsType.APINFO);  // Working
		        	t.addPollStat(OFStatisticsType.CLIENT); // Crashing
		        	t.addPollStat(OFStatisticsType.STATION); // Working
		        	t.addPollStat(OFStatisticsType.UTIL); // Working
		        	t.addPollStat(OFStatisticsType.TRAFFICINFO); // Working
		        	
		        	// Not used with the new router.
		        	t.addPollStat(OFStatisticsType.PASSIVE); // Passive crashing, Also driver not supported on the new router.
		        	t.addPollStat(OFStatisticsType.NONWIFI); // Working

		        	//t.addPollStat(OFStatisticsType.SYNCBEACON); // Working
		        	//t.removePollStat(OFStatisticsType.BEACON); // Don't collect beacon info for now.
			        activeThreads.add(t);
			        t.start();
			    }
			    
			    // Join all the threads after the timeout. Set a hard timeout
			    // of 12 seconds for the threads to finish. If the thread has not
			    // finished the switch has not replied yet and therefore we won't 
			    // add the switch's stats to the reply.
			    for (int iSleepCycles = 0; iSleepCycles < 12; iSleepCycles++) {
			        for (StatsQueryThread curThread : activeThreads) {
			            if (curThread.getState() == State.TERMINATED) {
			            	
			            	List<OFStatistics> apInfoStats = curThread.getApInfoStats();
			            	List<OFStatistics> utilStats = curThread.getUtilStats();
			            	List<OFStatistics> stationStats = curThread.getStationStats();
			        		List<OFStatistics> beaconStats = curThread.getBeaconStats();
			        		List<OFStatistics> clientStats = curThread.getClientStats();
			        		List<OFStatistics> nonWiFiStats = curThread.getNonWiFiStats();
			        		List<OFStatistics> passiveStats = curThread.getPassiveStats();
			        		List<OFStatistics> trafficinfoStats = curThread.getTrafficinfoStats();
			        		
			            	int apId = (int) curThread.apId;
			            	
			            	// Process the AP information statistics.
			            	// TODO: 0501 Bug for is5GhzSupported value. 
			            	CoapDataManager.processApinfoStats(apInfoStats, apId);
			            	
			            	// Process the airtime utilization statistics.
			            	CoapDataManager.processAirtimeUtilStats(utilStats, apId);
			            	
			                // Process the packet transmission statistics of local clients.
			            	CoapDataManager.processStationStats(stationStats, apId);
			                
			            	// Process information about the neighboring APs observed by the current AP.
			            	CoapDataManager.processBeaconStats(beaconStats, apId);
			                
			            	// Process additional information about the clients (e.g., hostname, device Id etc.)
			            	CoapDataManager.processClientStats(clientStats, apId);

			            	// Process the non-WiFi activity observed the AP.
			                CoapDataManager.processNonwifiStats(nonWiFiStats, apId);
			            	
			            	// Process aggregate statistics about the neighboring WiFi activity observed by the AP.
			                CoapDataManager.processPassiveStats(passiveStats, apId);
			                
			            	// Process the traffic related statistics.
			                CoapDataManager.processTrafficinfoStats(trafficinfoStats, apId);
			            	
			            	pendingRemovalThreads.add(curThread);
			            }
			        }
			        
			        // remove the threads that have completed the queries to the switches
			        for (StatsQueryThread curThread : pendingRemovalThreads) {
			            activeThreads.remove(curThread);
			        }
			        // clear the list so we don't try to double remove them
			        pendingRemovalThreads.clear();
			        
			        // if we are done finish early so we don't always get the worst case
			        if (activeThreads.isEmpty()) {
			            break;
			        }
			        
			        // sleep for 1 s here
			        try {
			            Thread.sleep(1000);
			        } catch (InterruptedException e) {
			            log.error("Interrupted while waiting for statistics", e);
			        }
			    }
			} catch (Exception e) {
				log.error("Error in CoapEngine main thread: " + e);
				e.printStackTrace();
			}
		}
    }
		
	/**
	 * Runs a thread to poll COAP related statistics from a single AP. 
	 * The pollStatsBitmap bitmap specifies the type of statistics that
	 * will be polled from the AP.
	 * 
	 * @author "Ashish Patro"
	 *
	 */
	protected class StatsQueryThread extends Thread {
		// Logger.
	    protected Logger log = 
	        LoggerFactory.getLogger(StatsQueryThread.class);
	    
		private long switchId;
        private int apId;
        
        private List<OFStatistics> apInfoStats;
        private List<OFStatistics> utilStats;
		private List<OFStatistics> stationStats;
		private List<OFStatistics> beaconStats;
		private List<OFStatistics> clientStats;
		private List<OFStatistics> nonWiFiStats;
		private List<OFStatistics> passiveStats;
		private List<OFStatistics> trafficinfoStats;

		private long pollStatsBitmap;

		public StatsQueryThread(long switchId, int apId, long pollStatsBitmap) {
            this.switchId = switchId;
            this.apId = apId;
            
            this.pollStatsBitmap = pollStatsBitmap;
        }
		
		public long getPollStatsBitmap() {
			return pollStatsBitmap;
		}

		public void setPollStatsBitmap(long pollStatsBitmap) {
			this.pollStatsBitmap = pollStatsBitmap;
		}
		
		public void removePollStat(OFStatisticsType statType) {
			this.pollStatsBitmap ^= (1 << statType.ordinal());
		}
		
		public void addPollStat(OFStatisticsType statType) {
			this.pollStatsBitmap |= (1 << statType.ordinal());
		}
        
        public List<OFStatistics> getApInfoStats() {
			return apInfoStats;
		}
        
        public List<OFStatistics> getUtilStats() {
			return utilStats;
		}

		public List<OFStatistics> getStationStats() {
			return stationStats;
		}
		
		public List<OFStatistics> getBeaconStats() {
			return beaconStats;
		}

		public List<OFStatistics> getClientStats() {
			return clientStats;
		}

		public List<OFStatistics> getNonWiFiStats() {
			return nonWiFiStats;
		}

		public List<OFStatistics> getPassiveStats() {
			return passiveStats;
		}
		
        public List<OFStatistics> getTrafficinfoStats() {
			return trafficinfoStats;
		}
        
		@Override
		public void run() {
        	
			IOFSwitch sw = floodlightProvider.getAllSwitchMap().get(switchId);

        	log.info("pollStatsBitmap: " + pollStatsBitmap + " " + (pollStatsBitmap & (1 << OFStatisticsType.BEACON.ordinal())));
        	long duration = System.currentTimeMillis();
        	
        	apInfoStats = (pollStatsBitmap & (1 << OFStatisticsType.APINFO.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.APINFO) : null;
        	utilStats = (pollStatsBitmap & (1 << OFStatisticsType.UTIL.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.UTIL) : null;
        	stationStats = (pollStatsBitmap & (1 << OFStatisticsType.STATION.ordinal())) > 0? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.STATION) : null;
        	nonWiFiStats = (pollStatsBitmap & (1 << OFStatisticsType.NONWIFI.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.NONWIFI) : null;
        	beaconStats = (pollStatsBitmap & (1 << OFStatisticsType.BEACON.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.BEACON) : null;
        	clientStats = (pollStatsBitmap & (1 << OFStatisticsType.CLIENT.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.CLIENT) : null;
        	passiveStats = (pollStatsBitmap & (1 << OFStatisticsType.PASSIVE.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.PASSIVE) : null;
        	
       		// Debug
    		trafficinfoStats = (pollStatsBitmap & (1 << OFStatisticsType.TRAFFICINFO.ordinal())) > 0 ? CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.TRAFFICINFO) : null;

        	duration = System.currentTimeMillis() - duration;
        	
        	log.info("Polling for apId " + apId + " took " + duration + " ms.");
        }
    }
}
