package net.floodlightcontroller.core.coap.dataparsers;

import java.util.*;

import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.AirtimeUtilStat;
import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFUtilStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse airtime utilization statistics received from the COAP APs, store and maintain
 * them for the COAP server.
 * 
 * @author "Ashish Patro"
 *
 */
public class AirtimeUtilParser implements Parser {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(AirtimeUtilParser.class);

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		OFUtilStatisticsReply ofUtilStat = (OFUtilStatisticsReply) currStat;

		AirtimeUtilStat currUtilStat = new AirtimeUtilStat(ofUtilStat.getChannel(), ofUtilStat.getTimestamp(),
				ofUtilStat.getUtilVal(), ofUtilStat.getActive(),
				ofUtilStat.getBusy(), ofUtilStat.getReceive(), 
				ofUtilStat.getXmit(), ofUtilStat.getNoiseFloor());

		synchronized (utilMapForStorage) {
			if (!utilMapForStorage.containsKey(apId)) {
				utilMapForStorage.put(apId, new HashMap<Long, AirtimeUtilStat>());
			}

			maxTs = Math.max(maxTs, currUtilStat.ts);
		}

		synchronized (utilInMemoryMap) {
			if (!utilInMemoryMap.containsKey(apId)) {
				utilInMemoryMap.put(apId, new ArrayList<AirtimeUtilStat>());
			}

			utilMapForStorage.get(apId).put(currUtilStat.ts, currUtilStat);
			utilInMemoryMap.get(apId).add(currUtilStat);
		}
	}

	@Override
	public Object getStorageHashMap() {
		return utilMapForStorage;
	}

	@Override
	public Object getInMemoryHashMap() {
		return utilInMemoryMap;
	}

	/**
	 * Stores airtime utilization information data before it is committed to a persistent storage.
	 */
	Map<Integer, HashMap<Long, AirtimeUtilStat> > utilMapForStorage = 
			new HashMap<Integer, HashMap<Long,AirtimeUtilStat>>();
	
	/**
	 * Stores airtime utilization information in memory for immediate processing requirements.
	 */
	Map<Integer, ArrayList<AirtimeUtilStat>> utilInMemoryMap = 
			new HashMap<Integer, ArrayList<AirtimeUtilStat>>();

	@Override
	public void commit(long tsLimit) {
		try {
			String queryFormat = "insert into " + CoapConstants.UTIL_TABLE + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> objArrayVector = new ArrayList<ArrayList<Object>>();
			
			synchronized (utilMapForStorage) {
				Iterator<Map.Entry<Integer, HashMap<Long, AirtimeUtilStat>>> it = 
						utilMapForStorage.entrySet().iterator();

				while (it.hasNext()) {
					boolean dontRemove = false;
					Map.Entry<Integer, HashMap<Long, AirtimeUtilStat>> pair = it.next();
					int ap_id = pair.getKey();
					Iterator<Map.Entry<Long, AirtimeUtilStat>> tim_it = pair.getValue().entrySet().iterator();
					while (tim_it.hasNext()) {
						Map.Entry<Long, AirtimeUtilStat> pair_tim = tim_it.next();
						long ts = pair_tim.getKey();

						AirtimeUtilStat util_stats = pair_tim.getValue();
						ArrayList<Object> objArray = new ArrayList<Object>();
						objArray.add(ap_id);
						objArray.add(ts);
						objArray.add(util_stats.util);
						objArray.add(util_stats.activeTime);
						objArray.add(util_stats.busyTime);
						objArray.add(util_stats.recvTime);
						objArray.add(util_stats.transmitTime);
						objArray.add(util_stats.frequency);
						objArray.add(util_stats.noiseFloor);

						objArrayVector.add(objArray);

						tim_it.remove();
					}

					if (!dontRemove) {
						it.remove();
					}
				}

				//DatabaseCommitter.ExecuteQuery(queries);
				DatabaseCommitter.executeQuery(queryFormat, objArrayVector);
				//System.out.println("queries size = " + queries.size());
			}
		} catch (Exception e) {
			log.error("Exception while inserting util statistics into DB: " + e.getMessage());
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

		synchronized(utilInMemoryMap) {
			Iterator<Map.Entry<Integer, ArrayList<AirtimeUtilStat>>> utilIterator = utilInMemoryMap.entrySet().iterator();
			while (utilIterator.hasNext()) {
				Map.Entry<Integer, ArrayList<AirtimeUtilStat>> utilEntry = utilIterator.next();
				ArrayList<AirtimeUtilStat> utilArray = utilEntry.getValue();
				Iterator<AirtimeUtilStat> arrayIter = utilArray.iterator();

				while (arrayIter.hasNext()) {
					AirtimeUtilStat obj = arrayIter.next();
					if (obj.ts > tsLimit - CoapConstants.INMEMORY_DATA_INTERVAL_SEC) {
						break;
					}

					cnt ++;
					arrayIter.remove();
				}
			}

			System.out.println("ClearMaps: " + tsLimit + " removed " + cnt + " util entries...");
		}

		return cnt;
	}
}
