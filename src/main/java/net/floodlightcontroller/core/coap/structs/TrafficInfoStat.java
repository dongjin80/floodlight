package net.floodlightcontroller.core.coap.structs;

/**
 * Represents a client's (clientId) traffic activity from a single source (srcIp, type) 
 * 
 * @author "Ashish Patro"
 *
 */
public class TrafficInfoStat {
	
	public long ts;
	public int packetCount, numBytes, packetRetries;
	public String trafficId, type, tid, trafficInfo; // E.g., netflix, youtube
	
	public String clientId;
	
	public String srcIp, dstIp;
	public int srcPort, dstPort;
}
