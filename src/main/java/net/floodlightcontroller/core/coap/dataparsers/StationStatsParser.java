package net.floodlightcontroller.core.coap.dataparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.StationStats;
import net.floodlightcontroller.core.coap.structs.StationStatsPerClient;
import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFStationStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the per-client aggregate statistics about their MAC layer WiFi activity reported by
 * COAP APs. These statistics are maintained at the server to make configuration related decisions.   
 * 
 * @author "Ashish Patro"
 *
 */
public class StationStatsParser implements Parser {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(StationStatsParser.class);

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		OFStationStatisticsReply ofStationStat = (OFStationStatisticsReply) currStat;

		StationStats stationStat =  new StationStats(ofStationStat.getTimestamp(), ofStationStat.getPacketCount(),
				ofStationStat.getPacketRetries(), ofStationStat.getRetryString());
		String clientMac = ofStationStat.getClientMacid();
		Long sec = stationStat.ts;

		maxTs = Math.max(maxTs, sec);
		
		synchronized (stationStatsForStorageMap) {
			if (!stationStatsForStorageMap.containsKey(apId)) {
				stationStatsForStorageMap.put(apId, new HashMap<Long, HashMap<String, StationStats>>());
			}

			if (!stationStatsForStorageMap.get(apId).containsKey(sec)) {
				stationStatsForStorageMap.get(apId).put(sec, new HashMap<String, StationStats>());
			}

			stationStatsForStorageMap.get(apId).get(sec).put(clientMac, stationStat);
		}

		synchronized (stationStatsInMemoryMap) {
			boolean found = false; 

			if (!stationStatsInMemoryMap.containsKey(apId)) {
				stationStatsInMemoryMap.put(apId, new ArrayList<StationStatsPerClient>());
			}

			for (int i = 0; i < stationStatsInMemoryMap.get(apId).size(); i++) {
				if (clientMac.equals(stationStatsInMemoryMap.get(apId).get(i).clientId)) {
					stationStatsInMemoryMap.get(apId).get(i).statsList.add(stationStat);
					found = true;
					break;
				}
			}

			if (!found) {
				StationStatsPerClient obj = new StationStatsPerClient();
				obj.statsList = new ArrayList<StationStats>();
				obj.clientId = clientMac;
				obj.statsList.add(stationStat);
				stationStatsInMemoryMap.get(apId).add(obj);
			}
		}
	}

	@Override
	public synchronized Object getStorageHashMap() {
		synchronized (stationStatsForStorageMap) {
			return stationStatsForStorageMap;
		}
	}

	@Override
	public Object getInMemoryHashMap() {
		return stationStatsInMemoryMap;
	}

	/**
	 * Stores station statistics before it is committed to a persistent storage.
	 */
	HashMap<Integer, HashMap<Long, HashMap<String, StationStats> > > stationStatsForStorageMap = 
			new HashMap<Integer, HashMap<Long,HashMap<String,StationStats>>>();
	
	/**
	 *  Stores traffic information in memory for immediate processing requirements.
	 */
	HashMap<Integer, ArrayList<StationStatsPerClient>> stationStatsInMemoryMap = 
			new HashMap<Integer, ArrayList<StationStatsPerClient>>();

	@Override
	public void commit(long tsLimit) {
		try {
			//ArrayList<String> queries = new ArrayList<String>();
			String queryFormat = "insert into " + CoapConstants.STATION_TABLE + " VALUES(?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> params = new ArrayList<ArrayList<Object>>();
			
			synchronized (stationStatsForStorageMap) {
				Iterator<Map.Entry<Integer, HashMap<Long, HashMap<String, StationStats> > > > it = 
						stationStatsForStorageMap.entrySet().iterator();
			
				while (it.hasNext()) {
					boolean dontRemove = false;
			
					Map.Entry<Integer, HashMap<Long, HashMap<String, StationStats> > > pair = it.next();
					int apId = pair.getKey();
					Iterator<Map.Entry<Long, HashMap<String, StationStats> > > timeIt = pair.getValue().entrySet().iterator();
			
					while (timeIt.hasNext()) {
						Map.Entry<Long, HashMap<String, StationStats>> pair_tim = timeIt.next();
						long ts = pair_tim.getKey();
			
						Iterator<Map.Entry<String, StationStats>> linkIt = pair_tim.getValue().entrySet().iterator();
			
						while (linkIt.hasNext()) {
							Map.Entry<String, StationStats> pair_link = linkIt.next();
							String client = pair_link.getKey();
							StationStats stats = pair_link.getValue();
			
							ArrayList<Object> objArray = new ArrayList<Object>();
							objArray.add(apId);
							objArray.add(ts);
							objArray.add(client);
							objArray.add(stats.packetCount);
							objArray.add(stats.packetRetries);
							objArray.add(stats.rateString);
			
							params.add(objArray);
							linkIt.remove();
						}
			
						timeIt.remove();
					}
			
					if (!dontRemove) {
						it.remove();
					}
				}
			
				//DatabaseCommitter.ExecuteQuery(queries);
				DatabaseCommitter.executeQuery(queryFormat, params);
				//System.out.println("queries size = " + queries.size());
			}
		} catch (Exception e) {
			log.error("Exception while inserting station statistics into DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	long maxTs = 0;

	@Override
	public long getMaxTs() {
		return maxTs;
	}

	@Override
	public int ClearInMemoryData(long tsLimit) {
		int cnt = 0;

		synchronized(stationStatsInMemoryMap) {
			Iterator<Map.Entry<Integer, ArrayList<StationStatsPerClient>>> stationIterator = stationStatsInMemoryMap.entrySet().iterator();

			while (stationIterator.hasNext()) {
				Map.Entry<Integer, ArrayList<StationStatsPerClient>> stationEntry = stationIterator.next();
				ArrayList<StationStatsPerClient> stationArray = stationEntry.getValue();
				Iterator<StationStatsPerClient> arrayIter = stationArray.iterator();

				while (arrayIter.hasNext()) {
					StationStatsPerClient obj = arrayIter.next();
					Iterator<StationStats> statsTter = obj.statsList.iterator();

					while (statsTter.hasNext()) {
						Long ts = statsTter.next().ts;
						if (ts > tsLimit - CoapConstants.INMEMORY_DATA_INTERVAL_SEC) {
							break;
						}

						statsTter.remove();
						cnt ++;
					}
				}

				//utilIterator.remove();
			}

			System.out.println("ClearMaps: " + tsLimit + " removed " + cnt + " station entries...");
		}

		return cnt;
	}
}
