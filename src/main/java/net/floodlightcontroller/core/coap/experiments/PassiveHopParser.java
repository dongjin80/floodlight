/*
 *  Temporary code for debugging and experiments.
 */
package net.floodlightcontroller.core.coap.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.coap.OFPassiveStatisticsReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.coap.dataparsers.Parser;
import net.floodlightcontroller.core.coap.statsmanager.DatabaseCommitter;
import net.floodlightcontroller.core.coap.structs.PassiveStats;
import net.floodlightcontroller.core.coap.util.CoapConstants;

/**
 * Used for parsing the experimental statistics about external WiFi link activity collected passively by the COAP APs.
 * For these statistics, the COAP APs use a secondary wireless card that hops across different WiFi channels in a 
 * round robin fashion and passively collects small snapshots about WiFi link activity on each channel.
 *  
 * @author "Ashish Patro"
 *
 */
public class PassiveHopParser implements Parser {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(PassiveHopParser.class);

	@Override
	public void processOFStat(OFStatistics currStat, int apId) {
		OFPassiveStatisticsReply ofPassiveStat = (OFPassiveStatisticsReply) currStat;
		
		PassiveStats currPassiveStat = new PassiveStats(ofPassiveStat.getAveragePacketLength(), ofPassiveStat.getAverageRate(),
						ofPassiveStat.getRssi(), ofPassiveStat.getPacketCount(), 
						ofPassiveStat.getPacketRetries(), ofPassiveStat.getRetryString(), 
						ofPassiveStat.getChannel());
		
		String link = String.format("%s %s", ofPassiveStat.getSender(), ofPassiveStat.getReceiver());
		Long sec = (long) ofPassiveStat.getTimestamp();
		
		maxTs = Math.max(maxTs, sec);
		
		synchronized (passiveHopStatsMapForStorage) {
			if (!passiveHopStatsMapForStorage.containsKey(apId)) {
				passiveHopStatsMapForStorage.put(apId, new HashMap<Long, HashMap<Integer, HashMap<String, PassiveStats>>>());
			}
		
			if (!passiveHopStatsMapForStorage.get(apId).containsKey(sec)) {
				passiveHopStatsMapForStorage.get(apId).put(sec, new HashMap<Integer, HashMap<String,PassiveStats>>());
			}
		
			if (!passiveHopStatsMapForStorage.get(apId).get(sec).containsKey(currPassiveStat.channel)) {
				passiveHopStatsMapForStorage.get(apId).get(sec).put(currPassiveStat.channel, new HashMap<String,PassiveStats>());
			}
		
			passiveHopStatsMapForStorage.get(apId).get(sec).get(currPassiveStat.channel).put(link, currPassiveStat);
		}
	}

	@Override
	public Object getStorageHashMap() {
		return passiveHopStatsMapForStorage;
	}

	@Override
	public void commit(long ts_limit) {
		try {
			String queryFormat = "INSERT INTO " + CoapConstants.PASSIVE_HOP_TABLE + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			ArrayList<ArrayList<Object>> params = new ArrayList<ArrayList<Object>>();
			
			synchronized (passiveHopStatsMapForStorage) {
				Iterator<Map.Entry<Integer, HashMap<Long, HashMap<Integer, HashMap<String, PassiveStats> > > > > it = 
							passiveHopStatsMapForStorage.entrySet().iterator();
		
				while (it.hasNext()) {
					boolean dontRemove = false;
					Map.Entry<Integer, HashMap<Long, HashMap<Integer, HashMap<String, PassiveStats> > > > pair = it.next();
					int ap_id = pair.getKey();
					Iterator<Map.Entry<Long, HashMap<Integer, HashMap<String, PassiveStats> > > > tim_it = pair.getValue().entrySet().iterator();
		
					while (tim_it.hasNext()) {
						Map.Entry<Long, HashMap<Integer, HashMap<String, PassiveStats>>> pair_tim = tim_it.next();
						long ts = pair_tim.getKey();
						Iterator<Map.Entry<Integer, HashMap<String, PassiveStats>>> freq_it = pair_tim.getValue().entrySet().iterator();
		
						while (freq_it.hasNext()) {
							Map.Entry<Integer, HashMap<String, PassiveStats>> pair_freq = freq_it.next();
							Integer frequency = pair_freq.getKey();
							Iterator<Map.Entry<String, PassiveStats>> link_it = pair_freq.getValue().entrySet().iterator();
		
							while (link_it.hasNext()) {
								Map.Entry<String, PassiveStats> pair_link = link_it.next();
								String link = pair_link.getKey();
								String ap = link.split(" ")[0], client = link.split(" ")[1];
								PassiveStats stats = pair_link.getValue();
		
								ArrayList<Object> objArray = new ArrayList<Object>();
								objArray.add(ap_id);
								objArray.add(ts);
								objArray.add(frequency);
								objArray.add(ap);
								objArray.add(client);
								objArray.add(stats.bytesPerPacket);
								objArray.add(stats.avgRate);
								objArray.add(stats.avgRssi);
								objArray.add(stats.packetCount);
								objArray.add(stats.packetRetries);
								objArray.add(stats.rateString);
		
								params.add(objArray);
								//queries.add(query);
								link_it.remove();
							}
		
							freq_it.remove();
						}
		
						tim_it.remove();
					}
		
					if (!dontRemove) {
						it.remove();
					}
				}
		
				DatabaseCommitter.executeQuery(queryFormat, params);
				//System.out.println("queries size = " + queries.size());
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

	/**
	 * Stores passive statistics data before it is committed to a persistent storage.
	 */
	HashMap<Integer, HashMap<Long, HashMap<Integer, HashMap<String, PassiveStats> > > > passiveHopStatsMapForStorage = 
			new HashMap<Integer, HashMap<Long,HashMap<Integer,HashMap<String, PassiveStats>>>>();

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

