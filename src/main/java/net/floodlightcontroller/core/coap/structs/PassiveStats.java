package net.floodlightcontroller.core.coap.structs;

/**
 * Stores the aggregate statistics about the MAC layer activity of an
 * "over-heard" WiFi link. This is used to represent the activity of
 * neighboring WiFi links on the same/other WiFi channels as seen by
 * the observing AP.
 * 
 * @author "Ashish Patro"
 *
 */
public class PassiveStats {
	
	public PassiveStats(int bytesPerPacket, float avgRate,
			float avgRssi, int packetCnt, int packetRetries,
			String rate_string, int channel) {
		
		this.bytesPerPacket = bytesPerPacket;
		this.avgRate = avgRate;
		this.avgRssi = avgRssi;
		this.packetCount = packetCnt;
		this.packetRetries = packetRetries;
		this.rateString = rate_string;
		this.channel = channel;
	}
	
	public int bytesPerPacket;
	public float avgRate;
	public float avgRssi;
	public int packetCount;
	public int packetRetries;
	public String rateString;
	public int channel;
}
