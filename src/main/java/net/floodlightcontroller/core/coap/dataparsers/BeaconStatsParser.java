package net.floodlightcontroller.core.coap.dataparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.BeaconStat;
import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFBeaconStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse information about the neighboring beacons reported by the COAP APs. This information
 * is maintained in memory and periodically stored into a database.
 * 
 * @author "Ashish Patro"
 *
 */
public class BeaconStatsParser implements Parser {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(BeaconStatsParser.class);

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		OFBeaconStatisticsReply ofBeaconStat = (OFBeaconStatisticsReply) currStat;

		BeaconStat currBeacon = new BeaconStat(ofBeaconStat.getAp(), ofBeaconStat.getAvgRssi(), ofBeaconStat.getChannel(), ofBeaconStat.getTimestamp());

		String apMacId = ofBeaconStat.getAp();
		Long sec = (long) ofBeaconStat.getTimestamp();
		
		synchronized (beaconInforForStorageMap) {
			if (!beaconInforForStorageMap.containsKey(apId)) {
				beaconInforForStorageMap.put(apId, new HashMap<Long, HashMap<String, BeaconStat>>());
			}

			if (!beaconInforForStorageMap.get(apId).containsKey(sec)) {
				beaconInforForStorageMap.get(apId).put(sec, new HashMap<String, BeaconStat>());
			}

			beaconInforForStorageMap.get(apId).get(sec).put(apMacId, currBeacon);
			maxTs = Math.max(maxTs, sec);
		}
	}

	@Override
	public synchronized Object getStorageHashMap() {
		synchronized (beaconInforForStorageMap) {
			return beaconInforForStorageMap;
		}
	}

	/**
	 * Stores beacon information data before it is committed to a persistent storage.
	 */
	public HashMap<Integer, HashMap<Long, HashMap<String, BeaconStat> > > beaconInforForStorageMap = 
			new HashMap<Integer, HashMap<Long, HashMap<String, BeaconStat> > >();

	@Override
	public void commit(long tsLimit) {
		try {
			String queryFormat = "Insert into " + CoapConstants.BEACON_STATS_TABLE + " VALUES(?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> params = new ArrayList<ArrayList<Object>>();

			//System.out.println("commiting beacon stats");

			synchronized (beaconInforForStorageMap) {
				@SuppressWarnings("rawtypes")
				Iterator it = beaconInforForStorageMap.entrySet().iterator();
	
				while (it.hasNext()) {
	
					@SuppressWarnings("rawtypes")
					Map.Entry pairs = (Map.Entry)it.next();
					Integer ap_id = (Integer)pairs.getKey();
					@SuppressWarnings("unchecked")
					HashMap<Long, HashMap<String, BeaconStat> > tim_hashmap = 
					(HashMap<Long, HashMap<String, BeaconStat> >)pairs.getValue();
	
					@SuppressWarnings("rawtypes")
					Iterator timIt = tim_hashmap.entrySet().iterator();
	
					while (timIt.hasNext()) {
						@SuppressWarnings("rawtypes")
						Map.Entry tim_pairs = (Map.Entry)timIt.next();
						Long sec = (Long)tim_pairs.getKey();
	
						@SuppressWarnings("unchecked")
						HashMap<String, BeaconStat> client_map = 
						(HashMap<String, BeaconStat>)tim_pairs.getValue();
						@SuppressWarnings("rawtypes")
						Iterator client_it = client_map.entrySet().iterator();
	
						while (client_it.hasNext()) {
							@SuppressWarnings("rawtypes")
							Map.Entry client_pairs = (Map.Entry)client_it.next();
							String client_mac = (String)client_pairs.getKey();
							BeaconStat stat = (BeaconStat)client_pairs.getValue();
	
							ArrayList<Object> objArray = new ArrayList<Object>();
							objArray.add(ap_id);
							objArray.add(client_mac);
							objArray.add(sec);
							objArray.add(stat.avgRssi);
							objArray.add(stat.channel); // TODO
	
							params.add(objArray);
	
							client_it.remove();
						}
	
						timIt.remove();
					}
	
					it.remove();
				}
	
				//DatabaseCommitter.ExecuteQuery(queries);
				DatabaseCommitter.executeQuery(queryFormat, params);
				//System.out.println("sz = " + queries.size());
				//beacon_map.clear();
			}
		} catch (Exception e) {
			log.error("Exception while inserting beacon statistics into DB: " + e.getMessage());
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
