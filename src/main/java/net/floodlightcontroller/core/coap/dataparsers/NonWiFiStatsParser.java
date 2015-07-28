package net.floodlightcontroller.core.coap.dataparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.NonWiFiDevice;
import net.floodlightcontroller.core.coap.structs.NonWiFiDeviceType;
import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFNonWiFiStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse non-WiFi activity information received from the COAP APs. The COAP APs use AirShark (IMC 2011)
 * to detect nearby non-WiFi activity.  
 * 
 * @author "Ashish Patro"
 *
 */
public class NonWiFiStatsParser implements Parser {
	// Constants.
	public static int MAXIMUM_INACTIVE_DURATION_SEC = 30;

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(NonWiFiStatsParser.class);

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		OFNonWiFiStatisticsReply ofNonWiFiDevice = (OFNonWiFiStatisticsReply) currStat;

		NonWiFiDevice currNonWiFiDevice = new NonWiFiDevice(ofNonWiFiDevice.getTimestamp(),
						ofNonWiFiDevice.getStartTs(),
						ofNonWiFiDevice.getEndTs(),

						ofNonWiFiDevice.getDuration(),
						ofNonWiFiDevice.getRssi(),

						ofNonWiFiDevice.getStartBin(), ofNonWiFiDevice.getCenterBin(), ofNonWiFiDevice.getEndBin(),
						NonWiFiDeviceType.get(ofNonWiFiDevice.getDeviceType()),
						ofNonWiFiDevice.getChannel());

		synchronized (nonwifiDevicForStorageeMap) {
			String currId = currNonWiFiDevice.getKey();

			if (!nonwifiDevicForStorageeMap.containsKey(apId)) {
				nonwifiDevicForStorageeMap.put(apId, new HashMap<String, NonWiFiDevice>());
			}

			if (nonwifiDevicForStorageeMap.get(apId).containsKey(currId)) {
				//System.out.println("Removing : " + currId.toString());
				nonwifiDevicForStorageeMap.get(apId).remove(currId);
			}

			nonwifiDevicForStorageeMap.get(apId).put(currId, currNonWiFiDevice);

			//System.out.println("Num nonwifi " + currId.hashCode() + " " + nonwifi_device_map.get(ap_id).size());
			maxTs = Math.max(maxTs, currNonWiFiDevice.timeStamp);
		}
	}

	@Override
	public synchronized Object getStorageHashMap() {
		synchronized (nonwifiDevicForStorageeMap) {
			return nonwifiDevicForStorageeMap;
		}
	}

	/**
	 * Stores non-WiFi activity information data before it is committed to a persistent storage.
	 */
	public HashMap<Integer, HashMap<String, NonWiFiDevice > > nonwifiDevicForStorageeMap = 
			new HashMap<Integer, HashMap<String, NonWiFiDevice> >();

	@Override
	public void commit(long tsLimit) { // Time limit doesn't matter in this case.
		try {
			String queryFormat = "Insert into " + CoapConstants.AIRSHARK_STATS_TABLE +
					" VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> params = new ArrayList<ArrayList<Object>>();
			
			//System.out.println("commiting airshark stats");

			synchronized (nonwifiDevicForStorageeMap) {
				@SuppressWarnings("rawtypes")
				Iterator it = nonwifiDevicForStorageeMap.entrySet().iterator();
	
				while (it.hasNext()) {
					//int ret = 0;
					//boolean dontRemove = false;
	
					@SuppressWarnings("rawtypes")
					Map.Entry pairs = (Map.Entry)it.next();
					Integer ap_id = (Integer)pairs.getKey();
					@SuppressWarnings("unchecked")
					HashMap<String, NonWiFiDevice > dev_hashmap = 
					(HashMap<String, NonWiFiDevice >)pairs.getValue();
	
					@SuppressWarnings("rawtypes")
					Iterator dev_it = dev_hashmap.entrySet().iterator();
					while (dev_it.hasNext()) {
						@SuppressWarnings("rawtypes")
						Map.Entry tim_pairs = (Map.Entry)dev_it.next();
						//String devId = (String)tim_pairs.getKey();
						NonWiFiDevice stat = (NonWiFiDevice) tim_pairs.getValue();
	
						// 0223: Added to insert all entries.
						/*
					if ((CoapConstants.USE_DEBUG && (((System.currentTimeMillis() / 1000) - stat.endTs) < 10)) ||
							(!CoapConstants.USE_DEBUG && ((System.currentTimeMillis() / 1000) - stat.endTs) 
									> MAXIMUM_INACTIVE_DURATION_SEC)) {
						 */
	
						System.out.println("Time diff " + ((System.currentTimeMillis() / 1000) - stat.endTs) + " for device " + stat);
	
						if ((CoapConstants.USE_DEBUG && (((System.currentTimeMillis() / 1000) - stat.endTs) > 10))) {
	
							ArrayList<Object> objArray = new ArrayList<Object>();
							objArray.add(ap_id);
							objArray.add(stat.timeStamp);
							objArray.add(stat.type.getValue());
							objArray.add(stat.subbandFreq);
							objArray.add(stat.startFreq);
							objArray.add(stat.centerFreq);
							objArray.add(stat.endFreq);
							objArray.add(stat.startTs);
							objArray.add(stat.endTs);
							objArray.add(stat.duration);
							objArray.add(stat.rssi);
	
							//System.out.println("Adding: " + devId.startTs + " " + devId.type + " " + devId.subbandFreq);
							//System.out.println("Adding: " + devId + " - " + stat.centerFreq);
	
							params.add(objArray);
							dev_it.remove();
						}
					}
					System.out.println("Num nonwifi after clear: " + nonwifiDevicForStorageeMap.get(ap_id).size());
				}
	
				//DatabaseCommitter.ExecuteQuery(queries);
				DatabaseCommitter.executeQuery(queryFormat, params);
				//System.out.println("sz = " + queries.size());
			}
		} catch (Exception e) {
			log.error("Exception while inserting non-WiFi statistics into DB: " + e.getMessage());
			
			e.printStackTrace();
		}
	}

	long maxTs = 0;

	@Override
	public long getMaxTs() {
		return maxTs;
	}

	@Override
	public Object getInMemoryHashMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int ClearInMemoryData(long tsLimit) {
		// TODO Auto-generated method stub
		return 0;
	}
}
