package net.floodlightcontroller.core.coap.statsmanager;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.coap.util.CoapConstants;
import net.floodlightcontroller.core.coap.util.CoapQueryUtils;
import net.floodlightcontroller.core.coap.util.CoapUtils;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.protocol.statistics.coap.OFApinfoStatisticsReply;
import org.openflow.protocol.statistics.coap.OFBeaconStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class constitutes a part of "StatsManager" module of the COAP server.
 * 
 * It uses the overheard beacon information reported by COAP APs to construct an "neighborhood map"
 * of APs which represents other APs in the vicinity of each AP.
 * 
 * @author "Ashish Patro"
 *
 */
public class NeighborhoodMapManager implements Runnable {
	/*
	 * Variables.
	 */
	// Logger.
    private static Logger log = 
        LoggerFactory.getLogger(NeighborhoodMapManager.class);
    
    // Module dependencies
    private IFloodlightProviderService floodlightProvider;
    
	// MacID -> COAP AP mapping.
	private static HashMap<String, Integer> apIdToApinfoMap = new HashMap<String, Integer>();

	// COAP AP -> Set of neigboring COAP APs.
	private static HashMap<Integer, TreeSet<Integer>> coapApNeighborMap =
			new HashMap<Integer, TreeSet<Integer>>();

	// COAP AP -> All neighboring MAC IDs.
	private static HashMap<Integer, TreeSet<String>> apNeighbourMacsMap = new HashMap<Integer, TreeSet<String>>();
	
	// Process information about AP's own NICs.
	public static void updateApinformation(int apId, 
			List<OFStatistics> apInformationList) {

		for (int i = 0; i < apInformationList.size(); i++) {
			OFApinfoStatisticsReply ofApInfo = (OFApinfoStatisticsReply) apInformationList.get(i);
			
			apIdToApinfoMap.put(ofApInfo.getApMacId(), apId);
		}
	}
	
	/**
	 * Set the floodlight provider.
	 * 
	 * @param floodlightProvider
	 */
	public NeighborhoodMapManager(IFloodlightProviderService floodlightProvider) {
		this.floodlightProvider = floodlightProvider;
	}
	
	/**
	 * Process the beacons observed by a COAP AP.
	 * 
	 * @param apId
	 * @param beaconList
	 */
	public static void updateNeighboringAPs(int apId, List<OFStatistics> beaconList) {

		TreeSet<String> neighboringBeacons = new TreeSet<String>();
		
    	for (int i = 0; i < beaconList.size(); i++) {
    		OFBeaconStatisticsReply ofBeaconStat = (OFBeaconStatisticsReply) beaconList.get(i);
    		
    		neighboringBeacons.add(ofBeaconStat.getAp());
    	}
    	
    	apNeighbourMacsMap.remove(apId);
    	apNeighbourMacsMap.put(apId, neighboringBeacons);
	}
	
	/**
	 * Build the neighborhood map.
	 */
	public static void buildCoapNeigbhourhoodMap() {
		
		for (Integer apId: apNeighbourMacsMap.keySet()) {
			TreeSet<Integer> neighboringCoapAps = new TreeSet<Integer>();
			TreeSet<String> beaconMacIds = apNeighbourMacsMap.get(apId);
		
			for (String beaconMac: beaconMacIds) {
				if (apIdToApinfoMap.containsKey(beaconMac)) {
					int otherApId = apIdToApinfoMap.get(beaconMac);
					
					// TODO 0501: Is this really needed?
					if (apId != otherApId) {
						neighboringCoapAps.add(otherApId);
					}
				}
			}
			
			coapApNeighborMap.put(apId, neighboringCoapAps);
		}
	}
	
	/**
	 * Print the list of AP ID and MAC ID tuples.
	 */
	public static void printApIdToApinfoMap() {
		log.info("NeighborhoodMapManager: Printing the apIdToApinfoMap map...");
		
		for (String macId: apIdToApinfoMap.keySet()) {
			Integer apId = apIdToApinfoMap.get(macId);

			log.info("*** " + apId + " " + macId);
		}
	}

	/**
	 * Print MAC IDs for the neighboring COAP APs for each COAP AP.
	 */
	public static void printApNeighbourMacsMap() {
		log.info("NeighborhoodMapManager: Printing the apNeighbourMacsMap map...");
		
		for (Integer apId: apNeighbourMacsMap.keySet()) {
			TreeSet<String> neighboringMacs = apNeighbourMacsMap.get(apId);

			for (String macId: neighboringMacs) {
				
				if (macId.startsWith("30")) {
					log.info("---> " + apId + " " + macId);
				}
			}
			log.info("---> ");
		}
	}
	
	/**
	 * Print the neighborhood map for nearby COAP APs using the beacons overhead by the COAP APs.
	 */
	public static void printCoapNeigbhourhoodMap() {
		log.info("NeighborhoodMapManager: Printing the neighborhood map...");
		
		for (Integer apId: coapApNeighborMap.keySet()) {
			TreeSet<Integer> neighboringCoapAps = coapApNeighborMap.get(apId);

			String apNeighbors = apId + " ->";
			
			for (Integer otherApId: neighboringCoapAps) {
				apNeighbors += " " + otherApId;
			}
			
			log.info(apNeighbors);
		}
	}

	/* Executes a thread to poll overheard beacon information from the COAP APs. 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		long beaconLoopCount = 0;
		
		while (true) {
			try {
				// Finalize timestamp.
				Thread.sleep(CoapConstants.DATA_POLL_FREQUENCY_MSEC);

				log.info("NeighborhoodMapManager: Poll beacons from APs...");

				Long[] switchDpids = floodlightProvider.getAllSwitchMap().keySet().toArray(new Long[0]);
				List<BeaconQueryThread> activeThreads = new ArrayList<BeaconQueryThread>(switchDpids.length);
				List<BeaconQueryThread> pendingRemovalThreads = new ArrayList<BeaconQueryThread>();

				BeaconQueryThread t;

			    // 0501 -> For debugging and preventing simultaneous scans.
			    beaconLoopCount = (beaconLoopCount + 1) % (switchDpids.length + 1);
			    int count = 0;
			    
				for (Long l : switchDpids) {
					
		        	// Avoid synchronized queries for beacons
		        	// 0501 -> Testing neighborhood map.
		        	count ++;
		        	
		        	if  (count == beaconLoopCount) {
		        		IOFSwitch sw = floodlightProvider.getAllSwitchMap().get(l);
						int apId = CoapUtils.getTestApIdFromRemoteIp(sw.getInetAddress().toString().toString());

						//log.info("Found " + sw.getInetAddress() + " switchDpid: " + l);

		        		log.info("Sending beacon poll to " + apId);
		        		
						t = new BeaconQueryThread(sw, apId); /// CoapConstants.POLL_ALL_COAP_STATS);
						activeThreads.add(t);
						t.start();
		        	}
				}

				// Join all the threads after the timeout. Set a hard timeout
				// of 12 seconds for the threads to finish. If the thread has not
				// finished the switch has not replied yet and therefore we won't 
				// add the switch's stats to the reply.
				for (int iSleepCycles = 0; iSleepCycles < 12; iSleepCycles++) {
					for (BeaconQueryThread curThread : activeThreads) {
						if (curThread.getState() == State.TERMINATED) {

							List<OFStatistics> beaconStats = curThread.getBeaconStats();

							int apId = (int) curThread.apId;

							// For database.
							 CoapDataManager.processBeaconStats(beaconStats, apId);
							
							// For neighborhoodMap.
							NeighborhoodMapManager.updateNeighboringAPs(apId, beaconStats);

							pendingRemovalThreads.add(curThread);
						}
					}

					// remove the threads that have completed the queries to the switches
					for (BeaconQueryThread curThread : pendingRemovalThreads) {
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

				// Code for maintaining the neighborhood map.
				// TODO 0501 - For testing.
				// TODO 0501 - Realized that this may work well if all APs do simultaneous scan.
				NeighborhoodMapManager.buildCoapNeigbhourhoodMap(); // Build the neighborhood map.
				
				NeighborhoodMapManager.printApNeighbourMacsMap();
				NeighborhoodMapManager.printApIdToApinfoMap(); 
				NeighborhoodMapManager.printCoapNeigbhourhoodMap();	// Print the neighborhood map.
				
			} catch (Exception e) {
				log.error("Error in NeighborhoodMapManager main thread: " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Creates and executes a thread to poll beacons overheard by the input COAP AP.
	 * 
	 * @author "Ashish Patro"
	 *
	 */
	protected class BeaconQueryThread extends Thread {
		// Logger.
	    protected Logger log = 
	        LoggerFactory.getLogger(BeaconQueryThread.class);
	    
		private IOFSwitch sw;
        private int apId;
        
		private List<OFStatistics> beaconStats;

		public BeaconQueryThread(IOFSwitch sw, int apId) {
            this.sw = sw;
            this.apId = apId;
        }
		
		public List<OFStatistics> getBeaconStats() {
			return beaconStats;
		}
        
		@Override
		public void run() {
        	long duration = System.currentTimeMillis();
        	beaconStats = CoapQueryUtils.getSwitchStatistics(sw, OFStatisticsType.BEACON);
        	log.info("Polling for apId " + apId + " took " + duration + " ms.");
        }
    }
}
