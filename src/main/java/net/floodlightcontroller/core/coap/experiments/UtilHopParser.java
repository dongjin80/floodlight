/*
 *  Temporary code for debugging and experiments.
 */
package net.floodlightcontroller.core.coap.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openflow.protocol.statistics.OFStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.coap.dataparsers.Parser;
import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.AirtimeUtilStat;
import net.floodlightcontroller.core.coap.util.CoapConstants;

/**
 * Used for parsing the experimental statistics about airtime utilization on different channels collected by the COAP APs.
 * For these statistics, the COAP APs use a secondary wireless card that hops across different WiFi channels in a round 
 * robin fashion and collects airtime utilization information on each channel. Typically measurement duration on each WiFi 
 * channel is 500ms. 
 * 
 * @author "Ashish Patro"
 *
 */
public class UtilHopParser implements Parser {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(UtilHopParser.class);

	public void process(String rest, int apId) {
		int cnt = Integer.parseInt(rest.substring(0, 2).replace("^0", ""));
		if (cnt == 0) {
			return;
		}

		rest = rest.substring(3);
		String[] freqTerms = rest.split(";");

		for (int i = 0; i < cnt; ++i) {

			String[] terms = freqTerms[i].split(" ");

			//System.out.println("utilhop freq_terms " + i + " :" + freq_terms[i] + "@" +
			//		terms[6] + "XX");

			int frequency = Integer.parseInt(terms[0]);
			int activeTime = Integer.parseInt(terms[1]);
			int busyTime = Integer.parseInt(terms[2]);
			int recvTime = Integer.parseInt(terms[3]);
			int transmitTime = Integer.parseInt(terms[4]);
			long sec = Long.parseLong(terms[5]);
			int noiseFloor = Integer.parseInt(terms[6]);

			if (activeTime < 50) {
				continue;
			}

			AirtimeUtilStat stats = new AirtimeUtilStat();
			stats.activeTime = activeTime;
			stats.recvTime = recvTime;
			stats.busyTime = busyTime;
			stats.transmitTime = transmitTime;
			stats.frequency = frequency;
			stats.noiseFloor = noiseFloor;
			stats.ts = sec;

			updateMap(apId, sec, stats);
		}

	}

	public void updateMap(int apId, Long sec, AirtimeUtilStat obj) {

		maxTs = Math.max(maxTs, sec);

		synchronized (utilHopMapForstorage) {
			if (!utilHopMapForstorage.containsKey(apId)) {
				utilHopMapForstorage.put(apId, new HashMap<Long, HashMap<Integer, ArrayList<AirtimeUtilStat>>>());
			}

			if (!utilHopMapForstorage.get(apId).containsKey(sec)) {
				utilHopMapForstorage.get(apId).put(sec, new HashMap<Integer, ArrayList<AirtimeUtilStat>>());
			}

			if (!utilHopMapForstorage.get(apId).get(sec).containsKey(obj.frequency)) {
				utilHopMapForstorage.get(apId).get(sec).put(obj.frequency, new ArrayList<AirtimeUtilStat>());
			}

			utilHopMapForstorage.get(apId).get(sec).get(obj.frequency).add(obj);
		}

		synchronized (utilHopInMemoryMap) {
			if (!utilHopInMemoryMap.containsKey(apId)) {
				utilHopInMemoryMap.put(apId, new ArrayList<AirtimeUtilStat>());
			}

			utilHopInMemoryMap.get(apId).add((AirtimeUtilStat) obj);
		}
	}

	@Override
	public Object getStorageHashMap() {
		return utilHopMapForstorage;
	}

	@Override
	public Object getInMemoryHashMap() {
		return utilHopInMemoryMap;
	}

	@Override
	public void commit(long tsLimit) {
		try {
			String queryFormat = "INSERT INTO " + CoapConstants.UTIL_HOP_TABLE + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> objArrayVector = new ArrayList<ArrayList<Object>>();
			
			synchronized (utilHopMapForstorage) {
				Iterator<Map.Entry<Integer, HashMap<Long, HashMap<Integer, ArrayList<AirtimeUtilStat> > > > > it = 
						utilHopMapForstorage.entrySet().iterator();

				while (it.hasNext()) {
					Map.Entry<Integer, HashMap<Long, HashMap<Integer, ArrayList<AirtimeUtilStat> > > > pair = it.next();
					int ap_id = pair.getKey();
					Iterator<Map.Entry<Long, HashMap<Integer, ArrayList<AirtimeUtilStat> > > > tim_it = pair.getValue().entrySet().iterator();
					
					while (tim_it.hasNext()) {
						Map.Entry<Long, HashMap<Integer, ArrayList<AirtimeUtilStat>>> pair_tim = tim_it.next();
						long ts = pair_tim.getKey();

						Iterator<Map.Entry<Integer, ArrayList<AirtimeUtilStat>>> freq_it = pair_tim.getValue().entrySet().iterator();
						while (freq_it.hasNext()) {
							Map.Entry<Integer, ArrayList<AirtimeUtilStat>> pair_freq = freq_it.next();
							ArrayList<AirtimeUtilStat> util_stats_list = pair_freq.getValue();
							for (AirtimeUtilStat stats: util_stats_list) {
								ArrayList<Object> objArray = new ArrayList<Object>();
								objArray.add(ap_id);
								objArray.add(stats.frequency);
								objArray.add(ts);
								objArray.add(stats.activeTime);
								objArray.add(stats.busyTime);
								objArray.add(stats.recvTime);
								objArray.add(stats.transmitTime);
								objArray.add(stats.noiseFloor);

								objArrayVector.add(objArray);
							}

							freq_it.remove();
						}

						tim_it.remove();
					}

					it.remove();
				}

				DatabaseCommitter.executeQuery(queryFormat, objArrayVector);
				//System.out.println("queries size = " + queries.size());
			}
		} catch (Exception e) {
			log.error("Exception while inserting utilHop statistics into DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	long maxTs = 0;

	@Override
	public long getMaxTs() {
		return maxTs;
	}

	/**
	 * Stores traffic information data before it is committed to a persistent storage.
	 */
	HashMap<Integer, HashMap<Long, HashMap<Integer, ArrayList<AirtimeUtilStat>>> > utilHopMapForstorage =
			new HashMap<Integer, HashMap<Long, HashMap<Integer, ArrayList<AirtimeUtilStat>>>>();
	
	/**
	 * Stores traffic information in memory for immediate processing requirements.
	 */
	public HashMap<Integer, ArrayList<AirtimeUtilStat>> utilHopInMemoryMap = 
			new HashMap<Integer, ArrayList<AirtimeUtilStat>>();

	@Override
	public int ClearInMemoryData(long tsLimit) {
		int cnt = 0;

		synchronized(utilHopInMemoryMap) {
			Iterator<Map.Entry<Integer, ArrayList<AirtimeUtilStat>>> utilHopIterator = utilHopInMemoryMap.entrySet().iterator();

			while (utilHopIterator.hasNext()) {
				Map.Entry<Integer, ArrayList<AirtimeUtilStat>> utilHopEntry = utilHopIterator.next();
				ArrayList<AirtimeUtilStat> utilHopArray = utilHopEntry.getValue();
				Iterator<AirtimeUtilStat> arrayIter = utilHopArray.iterator();

				while (arrayIter.hasNext()) {
					AirtimeUtilStat obj = arrayIter.next();

					if (obj.ts > tsLimit - CoapConstants.INMEMORY_DATA_INTERVAL_SEC) {
						break;
					}

					cnt ++;
					arrayIter.remove();
				}

				//utilHopIterator.remove();
			}

			System.out.println("ClearMaps: " + tsLimit + " removed " + cnt + " utilhop entries...");
		}

		return cnt;
	}

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		// TODO Auto-generated method stub

	}
}
