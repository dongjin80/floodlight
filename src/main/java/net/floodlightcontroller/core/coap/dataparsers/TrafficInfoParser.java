package net.floodlightcontroller.core.coap.dataparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.TrafficInfoPerClient;
import net.floodlightcontroller.core.coap.structs.TrafficInfoStat;
import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.openflow.protocol.statistics.OFStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse the traffic information statistics received from COAP APs, store and maintain them 
 * for the COAP server.
 * 
 * @author "Ashish Patro"
 *
 */
public class TrafficInfoParser implements Parser {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(TrafficInfoParser.class);

	/**
	 * Process the input string containing the traffic related information.
	 * 
	 * @param statString
	 * @param apId
	 */
	public void process(String statString, int apId) {

		int cnt = Integer.parseInt(statString.substring(0, 2));
		if (cnt == 0) {
			return;
		}

		statString = statString.substring(3);
		String[] terms = statString.split(";");
		assert(cnt == terms.length);

		for (int i = 0; i < cnt; ++i) {
			// Old example: 28:CF:E9:18:14:C1 2915184226 3232236431 80 53080 TCP 36 47610 1392313564
			// New example: 99514018ec7a996121 711e9530338 514fcb67d0a 443 58859 TCP google^google_inc.^us 159 223008 1425159713
			String curr = terms[i];
			String client, type;
			String trafficinfo[];
			String[] currTerms = curr.split(" ");

			//System.out.println("higherlayer term " + i + " :" + terms[i] + "--" + curr_terms[6] + "XX");

			client = currTerms[0];

			// TODO: Not used
			String srcIP = currTerms[1];
			String dstIP = currTerms[2];

			type = currTerms[5];
			trafficinfo = currTerms[6].split("\\^");

			int srcPort = 0, dstPort = 0, packetCnt = 0, numBytes = 0, num_retries = 0;
			long sec = 0;

			if (!type.equals("ICMP")) {
				srcPort = Integer.parseInt(currTerms[3]);
				dstPort = Integer.parseInt(currTerms[4]);
			}
			
			packetCnt = Integer.parseInt(currTerms[7]);
			numBytes = Integer.parseInt(currTerms[8]);
			sec = Long.parseLong(currTerms[9]);

			// TODO: Not using retries for now.
			// int num_retries = Integer.parseInt(curr_terms[5]);

			TrafficInfoStat stats = new TrafficInfoStat();

			// 0216: Added client info to get a direct reference, pretty inefficient otherwise.
			stats.clientId = client.toLowerCase();

			stats.srcIp = srcIP;
			stats.dstIp = dstIP;
			stats.srcPort = srcPort;
			stats.dstPort = dstPort;
			
			stats.ts = sec;
			stats.numBytes = numBytes;
			stats.packetCount = packetCnt;
			stats.packetRetries = num_retries; // Unused
			
			// TODO: 0218: Added to get more information about the traffic flow.
			// stats.type = typ + " " + srcPort + " " + dstPort;
			stats.type = type;
			stats.tid = trafficinfo[0];
			stats.trafficInfo = trafficinfo[1] + "^" + trafficinfo[2];


			updateMap(apId, sec, client, stats);
		}
	}

	/**
	 * Update the local structures with the input statistics.
	 * 
	 * @param apId
	 * @param sec
	 * @param clientMac
	 * @param o
	 */
	public void updateMap(int apId, Long sec, String clientMac, Object o) {
		maxTs = Math.max(maxTs, sec);
		
		synchronized (trafficinfoForStorageMap) {
			if (!trafficinfoForStorageMap.containsKey(apId)) {
				trafficinfoForStorageMap.put(apId, new HashMap<Long, ArrayList<TrafficInfoStat>>());
			}

			if (!trafficinfoForStorageMap.get(apId).containsKey(sec)) {
				trafficinfoForStorageMap.get(apId).put(sec, new ArrayList<TrafficInfoStat>());
			}

			trafficinfoForStorageMap.get(apId).get(sec).add((TrafficInfoStat)o);
		}

		synchronized (trafficinfoInMemoryMap) {
			if (!trafficinfoInMemoryMap.containsKey(apId)) {
				trafficinfoInMemoryMap.put(apId, new ArrayList<TrafficInfoPerClient>());
			}

			boolean found = false;
			for (int i = 0; i < trafficinfoInMemoryMap.get(apId).size(); i++)	{
				if (clientMac.equals(trafficinfoInMemoryMap.get(apId).get(i).clientId)) {
					trafficinfoInMemoryMap.get(apId).get(i).statsList.add((TrafficInfoStat) o);
					found = true;
					break;
				}
			}

			if (!found) {
				TrafficInfoPerClient obj = new TrafficInfoPerClient();
				obj.statsList = new ArrayList<TrafficInfoStat>();
				obj.clientId = clientMac;
				obj.statsList.add((TrafficInfoStat)o);

				trafficinfoInMemoryMap.get(apId).add(obj);
			}
		}
	}

	@Override
	public synchronized Object getStorageHashMap() {
		synchronized (trafficinfoForStorageMap) {
			return trafficinfoForStorageMap;
		}
	}

	@Override
	public void commit(long tsLimit) {
		try {
			String queryFormat = "insert into " + CoapConstants.HIGHER_LAYER_TABLE + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> params = new ArrayList<ArrayList<Object>>();

			synchronized ( trafficinfoForStorageMap) {
				Iterator<Map.Entry<Integer, HashMap<Long, ArrayList<TrafficInfoStat> > > > it =
						trafficinfoForStorageMap.entrySet().iterator();
				
				while (it.hasNext()) {
					boolean dontRemove = false;
		
					Entry<Integer, HashMap<Long, ArrayList<TrafficInfoStat>>> pair = it.next();
					int ap_id = pair.getKey();
					
					Iterator<Entry<Long, ArrayList<TrafficInfoStat>>> tim_it = pair.getValue().entrySet().iterator();
					while (tim_it.hasNext()) {
						Entry<Long, ArrayList<TrafficInfoStat>> pair_tim = tim_it.next();
						long ts = pair_tim.getKey();
		
						Iterator<TrafficInfoStat> link_it = pair_tim.getValue().iterator();
						
						while (link_it.hasNext()) {
							TrafficInfoStat stats = link_it.next();
		
							ArrayList<Object> objArray = new ArrayList<Object>();

							objArray.add(ap_id);
							objArray.add(ts);
							objArray.add(stats.clientId);
							objArray.add(stats.srcIp);
							objArray.add(stats.dstIp);
							objArray.add(stats.srcPort);
							objArray.add(stats.dstPort);
							objArray.add(stats.type);
							objArray.add(stats.tid);
							objArray.add(stats.packetCount);
							objArray.add(stats.numBytes);
							objArray.add(stats.packetRetries);
							objArray.add(stats.trafficInfo);
							
							params.add(objArray);
							link_it.remove();
						}
		
						tim_it.remove();
					}
		
					if (!dontRemove) {
						it.remove();
					}
				}
		
				/*if (queries.size() > 0) {
				DatabaseCommitter.ExecuteQuery(queries);
				}*/
				DatabaseCommitter.executeQuery(queryFormat, params);
				//System.out.println("queries size = " + queries.size());
			}
		} catch (Exception e) {
			log.error("Exception while inserting trafficInfo statistics into DB: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Stores traffic information data before it is committed to a persistent storage.
	 */
	static HashMap<Integer, HashMap<Long, ArrayList<TrafficInfoStat> > > trafficinfoForStorageMap = 
			new HashMap<Integer, HashMap<Long, ArrayList<TrafficInfoStat>>>();
	
	/**
	 * Stores traffic information in memory for immediate processing requirements.
	 */
	static HashMap<Integer,  ArrayList<TrafficInfoPerClient>> trafficinfoInMemoryMap = 
			new HashMap<Integer, ArrayList<TrafficInfoPerClient>>();


	long maxTs = 0;

	@Override
	public long getMaxTs() {
		return maxTs;
	}

	@Override
	public Object getInMemoryHashMap() {
		return trafficinfoInMemoryMap;
	}

	@Override
	public int ClearInMemoryData(long tsLimit) {
		int cnt = 0;

		synchronized(trafficinfoInMemoryMap) {
			Iterator<Map.Entry<Integer, ArrayList<TrafficInfoPerClient>>> higherLayerIterator = trafficinfoInMemoryMap.entrySet().iterator();
			while (higherLayerIterator.hasNext()) {
				Map.Entry<Integer, ArrayList<TrafficInfoPerClient>> higherLayerEntry = higherLayerIterator.next();
				ArrayList<TrafficInfoPerClient> higherLayerArray = higherLayerEntry.getValue();
				Iterator<TrafficInfoPerClient> arrayIter = higherLayerArray.iterator();

				while (arrayIter.hasNext()) {
					TrafficInfoPerClient obj = arrayIter.next();
					Iterator<TrafficInfoStat> stats_iter = obj.statsList.iterator();

					while (stats_iter.hasNext()) {
						Long ts = stats_iter.next().ts;
						if (ts > tsLimit - CoapConstants.INMEMORY_DATA_INTERVAL_SEC) {
							break;
						}

						stats_iter.remove();
						cnt ++;
					}
				}
			}

			System.out.println("ClearMaps: " + tsLimit + " removed " + cnt + " higher layer entries...");
		}

		return cnt;
	}

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		// TODO Auto-generated method stub

	}
}
