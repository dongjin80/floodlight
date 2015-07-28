package net.floodlightcontroller.core.coap.structs;

import java.util.ArrayList;

/**
 * Stores the time-series information about a client's MAC layer WiFi activity.
 *   
 * @author "Ashish Patro"
 *
 */
public class StationStatsPerClient {
	public String clientId;
	public ArrayList<StationStats> statsList;
}