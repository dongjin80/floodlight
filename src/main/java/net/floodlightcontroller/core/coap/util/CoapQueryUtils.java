package net.floodlightcontroller.core.coap.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.annotations.LogMessageDoc;

import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.protocol.statistics.coap.OFApinfoStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFBeaconStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFClientStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFDiagnosticinfoStatisticsReply;
import org.openflow.protocol.statistics.coap.OFDiagnosticinfoStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFNonWiFiStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFPassiveStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFStationStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFSyncBeaconStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFTrafficinfoStatisticsRequest;
import org.openflow.protocol.statistics.coap.OFUtilStatisticsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions for querying statistics from the COAP APs.
 * 
 * @author "Ashish Patro"
 *
 */
public class CoapQueryUtils {

	// Logger.
	protected static Logger log = 
			LoggerFactory.getLogger(CoapQueryUtils.class);

	/**
	 * Get a specific type of statistics from the input switch.
	 * 
	 * @param sw
	 * @param statType
	 * @return
	 */
	@LogMessageDoc(level="ERROR",
			message="Failure retrieving statistics from switch {switch}",
			explanation="An error occurred while retrieving statistics" +
					"from the switch",
					recommendation=LogMessageDoc.CHECK_SWITCH + " " +
							LogMessageDoc.GENERIC_ACTION)
	public static List<OFStatistics> getSwitchStatistics(IOFSwitch sw, 
			OFStatisticsType statType) {

		Future<List<OFStatistics>> future;
		List<OFStatistics> values = null;
		if (sw != null) {
			OFStatisticsRequest req = new OFStatisticsRequest();
			req.setStatisticType(statType);

			int requestLength = req.getLengthU();

			// TODO: 0123 Dummy value - not using the type field for now.
			if (statType == OFStatisticsType.APINFO) {
				OFApinfoStatisticsRequest specificReq = new OFApinfoStatisticsRequest();
				specificReq.setType((short) 0); 
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.UTIL) {
				OFUtilStatisticsRequest specificReq = new OFUtilStatisticsRequest();
				specificReq.setType((short) 0); 
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.STATION) {
				OFStationStatisticsRequest specificReq = new OFStationStatisticsRequest();
				specificReq.setType((short) 0); 
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.SYNCBEACON) {
				OFSyncBeaconStatisticsRequest specificReq = new OFSyncBeaconStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.BEACON) {
				OFBeaconStatisticsRequest specificReq = new OFBeaconStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.CLIENT) {
				OFClientStatisticsRequest specificReq = new OFClientStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.NONWIFI) {
				OFNonWiFiStatisticsRequest specificReq = new OFNonWiFiStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.PASSIVE) {
				OFPassiveStatisticsRequest specificReq = new OFPassiveStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.TRAFFICINFO) {
				OFTrafficinfoStatisticsRequest specificReq = new OFTrafficinfoStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();

			} else if (statType == OFStatisticsType.DIAGNOSTICINFO) {
				OFDiagnosticinfoStatisticsRequest specificReq = new OFDiagnosticinfoStatisticsRequest();
				specificReq.setType((short) 0);
				req.setStatistics(Collections.singletonList((OFStatistics)specificReq));
				requestLength += specificReq.getLength();
			}

			req.setLengthU(requestLength);
			try {
				future = sw.queryStatistics(req);
				values = future.get(10, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.error("Failure retrieving statistics from switch " + sw, e);
			}
		}
		return values;
	}

	/**
	 * Obtain diagnostic statistics from a COAP AP. The statistics 
	 * consist of packet-level summaries with packet transmission
	 * information. These statistics are used for interference detection.
	 * 
	 * TODO - Currently used to pull data more efficiently from the APs.
	 * 
	 * @param ipAddress
	 * @param diagnosticStats
	 */
	public static void ObtainDiagnosticStats(String ipAddress, List<OFStatistics> diagnosticStats) {
		String[] cmd = {
				"/bin/sh",
				"-c",
				"echo 'read passive_ap_state.stats' | nc " + ipAddress + " 8777 < /dev/stdin"
		};

		try {
			log.info("Getting pie results from " + ipAddress);

			BufferedReader input = new BufferedReader
					(new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));

			String line = "", output = "";

			// Get the last line.
			while ((line = input.readLine()) != null) {
				// output += (line + '\n');
				output = line;
			}
			input.close();

			//System.out.println("Result: " + output);

			OFDiagnosticinfoStatisticsReply currReply = new OFDiagnosticinfoStatisticsReply();
			currReply.setDiagnosticInfoStatsString(output);

			diagnosticStats.add(currReply);

		} catch (IOException e) {
			log.error("ObtainDiagnosticStats exception :( " + e.getMessage());
			e.printStackTrace();
		}
	}
}
