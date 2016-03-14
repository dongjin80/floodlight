/**
*    Copyright (c) 2008 The Board of Trustees of The Leland Stanford Junior
*    University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package org.openflow.protocol.statistics.coap;


import net.floodlightcontroller.core.coap.util.CoapConstants;

import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.util.StringByteSerializer;

/**
 * Represents an ofp_station_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFStationStatisticsReply implements OFStatistics {
	
    protected String clientMacid;
    protected String retryString;

    protected int packetCount;    // packet count
	protected int packetRetries;  // packet retries
    //uint16_t average_packet_length; // Average packet length
    //uint16_t average_rate_times10;          // average rate times 10

    protected int rssi; // Observed signal strength
    // uint32_t channel; // Current channel
    protected int timestamp; // unix timestamp in seconds

	public String getClientMacid() {
		return clientMacid;
	}

	public void setClientMacid(String clientMacid) {
		this.clientMacid = clientMacid;
	}

	public String getRetryString() {
		return retryString;
	}

	public void setRetryString(String retryString) {
		this.retryString = retryString;
	}

    public int getPacketCount() {
		return packetCount;
	}

	public void setPacketCount(int packetCount) {
		this.packetCount = packetCount;
	}

	public int getPacketRetries() {
		return packetRetries;
	}

	public void setPacketRetries(int packetRetries) {
		this.packetRetries = packetRetries;
	}
	
	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	@Override
    public int getLength() {
        return 196; //CoapConstants.STATION_STATS_LENGTH; //26880; //22784; //9472;
    }

    @Override
    public void readFrom(ChannelBuffer data) {
    	this.clientMacid = StringByteSerializer.readFrom(data,
    			CoapConstants.MAC_ID_STRING_LEN);
    	
    	this.retryString = StringByteSerializer.readFrom(data,
    			CoapConstants.RATE_STRING_LENGTH);

    	this.packetCount = data.readInt();
    	this.packetRetries = data.readInt();
    	this.rssi = data.readInt();
    	this.timestamp = data.readInt();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.clientMacid);

    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.retryString);
    	
    	data.writeInt(this.packetCount);
    	data.writeInt(this.packetRetries);
    	data.writeInt(this.rssi);
    	data.writeInt(this.timestamp);
    }

    @Override
	public String toString() {
		return "OFStationStatisticsReply [clientMacid=" + clientMacid
				+ ", retryString=" + retryString + ", packetCount="
				+ packetCount + ", packetRetries=" + packetRetries
				+ ", rssi=" + rssi + ", timestamp=" + timestamp + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 571;
        int result = 1;
        
        result = prime
                * result
                + ((clientMacid == null) ? 0 : clientMacid
                        .hashCode());

        result = prime
                * result
                + ((retryString == null) ? 0 : retryString
                        .hashCode());
        
        result = prime * result + this.packetCount;
        result = prime * result + (this.packetRetries ^ (this.packetRetries >>> 32));
        result = prime * result + (this.timestamp ^ (this.timestamp >>> 32));
        result = prime * result + (this.rssi ^ (this.rssi >>> 32));
         
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFStationStatisticsReply)) {
            return false;
        }
        
        OFStationStatisticsReply other = (OFStationStatisticsReply) obj;
        
        if (!this.clientMacid.equals(other.clientMacid)) {
            return false;
        }
        
        if (!this.retryString.equals(other.retryString)) {
            return false;
        }

        if (this.timestamp != other.timestamp) {
        	return false;
        }
        
        if (this.packetCount != other.packetCount) {
        	return false;
        }
        
        if (this.packetRetries != other.packetRetries) {
        	return false;
        }
        
        if (this.rssi != other.rssi) {
        	return false;
        }
        
        return true;
    }
}
