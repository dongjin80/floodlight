package net.floodlightcontroller.core.coap.structs;

/**
 * Store information about a beacon received from a neighboring AP.
 * 
 * @author "Ashish Patro"
 *
 */
public class BeaconStat {
	public String apId;
	public double avgRssi;
	public int channel;
	public long timestamp;
	
	public BeaconStat(String apId, double avgRssi, int channel, long timestamp) {
		this.apId = apId;
		this.avgRssi = avgRssi;
		this.channel = channel;
		this.timestamp = timestamp;
	}
}