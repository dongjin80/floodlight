package net.floodlightcontroller.core.coap.structs;

import net.floodlightcontroller.core.coap.util.CoapConstants;

/**
 * Store the airtime utilization information observed by a COAP AP on a single WiFi channel.
 * 
 * @author "Ashish Patro"
 *
 */
public class AirtimeUtilStat {
	public AirtimeUtilStat() {
		frequency = -1;
		activeTime = -1;
		busyTime = -1;
		recvTime = -1; 
		transmitTime = -1;
		util = -1;
		ts = 0;
		noiseFloor = CoapConstants.UNK_NOISE_FLOOR;
	}
	
	public AirtimeUtilStat(int frequency, long ts, double util, int activeTime,
			int busyTime, int recvTime, int transmitTime, int noiseFloor) {
		this.frequency = frequency;
		this.ts = ts;
		this.util = util;
		this.activeTime = activeTime;
		this.busyTime = busyTime;
		this.recvTime = recvTime;
		this.transmitTime = transmitTime;
		this.noiseFloor = noiseFloor;
	}

	public int frequency;
	public long ts;
	public double util;
	public int activeTime, busyTime, recvTime, transmitTime;
	public int noiseFloor;
}

