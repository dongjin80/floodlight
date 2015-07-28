package net.floodlightcontroller.core.coap.structs;

import java.util.ArrayList;

/**
 * Stores time-series data about a client's traffic usage activity. 
 * 
 * @author "Ashish Patro"
 *
 */
public class TrafficInfoPerClient {
	public TrafficInfoPerClient() {
		statsList = new ArrayList<TrafficInfoStat>();
	}

	public String clientId;
	public ArrayList<TrafficInfoStat> statsList;
}