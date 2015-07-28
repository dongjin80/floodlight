package net.floodlightcontroller.core.coap.structs;

/**
 * 
 * Example: 
 * Fixed: 1379038109 2013-09-12 21:08:29 110 2462 0 26 55 1379038096 1379038108 12 -73.5;
 * FreqHop: 1379038677 2013-09-12 21:17:57 106 2400 107 28 55 1379038671 1379038677 6 -66.4927
 */

/**
 * Stores a single instance of the non-WiFi device's activity observed by COAP AP using
 * AirShark (IMC 2011 paper) for non-WiFi device detection.
 * 
 * @author "Ashish Patro"
 *
 */
public class NonWiFiDevice {
	public long timeStamp;
	public long startTs;
	public long endTs;

	public long duration;
	public double rssi;
	public int startFreq, centerFreq, endFreq;
	//public byte startFreq, centerFreq, endFreq;
	public NonWiFiDeviceType type;

	public int subbandFreq;
	public boolean isInactive;

	public String getKey() {
		return startTs + " " + type + " " + subbandFreq;
	}

	@Override
	public String toString() {
		return "NonWiFiDevice [timeStamp=" + timeStamp + ", startTs=" + startTs
				+ ", endTs=" + endTs + ", duration=" + duration + ", rssi="
				+ rssi + ", startFreq=" + startFreq + ", centerFreq="
				+ centerFreq + ", endFreq=" + endFreq + ", type=" + type
				+ ", subbandFreq=" + subbandFreq + ", isInactive=" + isInactive
				+ "]";
	}

	public NonWiFiDevice(long timeStamp, long startTs, long endTs, long duration, double rssi,
			int startFreq, int centerFreq, int endFreq,
			//byte startFreq, byte centerFreq, byte endFreq,
			NonWiFiDeviceType type, int subbandFreq) {
		this.timeStamp = timeStamp;

		this.startTs = startTs;
		this.endTs = endTs;
		this.duration = duration;

		this.rssi = rssi;

		this.startFreq = startFreq;
		this.centerFreq = centerFreq;
		this.endFreq = endFreq;

		this.type = type;
		this.subbandFreq = subbandFreq;

		this.isInactive = false;
	}
}