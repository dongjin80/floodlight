package net.floodlightcontroller.core.coap.dataparsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.PassiveStats;
import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFPassiveStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse the statistics about external WiFi activity collected passively by the COAP APs. The statistics
 * provide information about the neighboring WiFi activity characteristics as seen by the COAP
 * AP (e.g., packets sent/retried, data rates used, signal strength etc.).
 * 
 * @author "Ashish Patro"
 *
 */
public class PassiveStatsParser implements Parser {
	
	// Logger.
	protected static Logger log = 
		LoggerFactory.getLogger(PassiveStatsParser.class);

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		OFPassiveStatisticsReply ofPassiveStat = (OFPassiveStatisticsReply) currStat;
		
		String link = String.format("%s %s", ofPassiveStat.getSender(), ofPassiveStat.getReceiver());
		PassiveStats currPassiveStat = new PassiveStats(ofPassiveStat.getAveragePacketLength(), ofPassiveStat.getAverageRate(), 
						ofPassiveStat.getRssi(), ofPassiveStat.getPacketCount(),
						ofPassiveStat.getPacketRetries(), ofPassiveStat.getRetryString(),
						ofPassiveStat.getChannel());		
		Long sec = (long) ofPassiveStat.getTimestamp(); 

		maxTs = Math.max(maxTs, sec);
		
		synchronized (passiveStatsForStorageMap) {
			if (!passiveStatsForStorageMap.containsKey(apId)) {
				passiveStatsForStorageMap.put(apId, new HashMap<Long, HashMap<String, PassiveStats> >());
			}

			if (!passiveStatsForStorageMap.get(apId).containsKey(sec)) {
				passiveStatsForStorageMap.get(apId).put(sec, new HashMap<String, PassiveStats>());
			}

			passiveStatsForStorageMap.get(apId).get(sec).put(link, currPassiveStat);
		}

	}

	@Override
	public synchronized Object getStorageHashMap() {
		return passiveStatsForStorageMap;
	}

	@Override
	public void commit(long tsLimit) {
		try {
			String queryFormat = "insert into " + CoapConstants.PASSIVE_TABLE + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> params = new ArrayList<ArrayList<Object>>();
		
			synchronized (passiveStatsForStorageMap) {
				Iterator<Map.Entry<Integer, HashMap<Long, HashMap<String, PassiveStats> > > > it = 
						passiveStatsForStorageMap.entrySet().iterator();
				
				while (it.hasNext()) {
					boolean dontRemove = false;
					Map.Entry<Integer, HashMap<Long, HashMap<String, PassiveStats> > > pair = it.next();
					int ap_id = pair.getKey();
					Iterator<Map.Entry<Long, HashMap<String, PassiveStats> > > tim_it = pair.getValue().entrySet().iterator();
					while (tim_it.hasNext()) {
						Map.Entry<Long, HashMap<String, PassiveStats>> pair_tim = tim_it.next();
						long ts = pair_tim.getKey();
						
						Iterator<Map.Entry<String, PassiveStats>> link_it = pair_tim.getValue().entrySet().iterator();
						while (link_it.hasNext()) {
							Map.Entry<String, PassiveStats> pair_link = link_it.next();
							String link = pair_link.getKey();
							PassiveStats stats = pair_link.getValue();
							String ap = link.split(" ")[0], client = link.split(" ")[1];
							
							ArrayList<Object> objArray = new ArrayList<Object>();
							objArray.add(ap_id);
							objArray.add(ts);
							objArray.add(ap);
							objArray.add(client);
							objArray.add(stats.bytesPerPacket);
							objArray.add(stats.avgRate);
							objArray.add(stats.avgRssi);
							objArray.add(stats.packetCount);
							objArray.add(stats.packetRetries);
							objArray.add(stats.rateString);
							objArray.add(stats.channel);
							params.add(objArray);
							link_it.remove();
						}
						
						tim_it.remove();
					}
					
					if (!dontRemove) {
						it.remove();
					}
				}
				
				//System.out.println("queries size = " + queries.size());
				//DatabaseCommitter.ExecuteQuery(queries);
				DatabaseCommitter.executeQuery(queryFormat, params);
			}
		} catch (Exception e) {
			log.error("Exception while inserting passive statistics into DB: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	long maxTs = 0;
	
	/**
	 * Stores the passive WiFi statistics before it is committed to a persistent storage.
	 */
	HashMap<Integer, HashMap<Long, HashMap<String, PassiveStats> > > passiveStatsForStorageMap = 
			new HashMap<Integer, HashMap<Long,HashMap<String,PassiveStats>>>();
	
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
