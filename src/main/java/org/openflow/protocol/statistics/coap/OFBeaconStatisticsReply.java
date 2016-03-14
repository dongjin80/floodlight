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
 * Represents an ofp_beacon_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFBeaconStatisticsReply implements OFStatistics {
    protected String ap; /* Identifier for the AP */
    protected int timestamp; /* unix timestamp in seconds */
    protected int avgRssi; /* Signal strength of the observed AP */
    protected short channel; /* The AP's current WiFi channel */

    public String getAp() {
		return ap;
	}

	public void setAp(String ap) {
		this.ap = ap;
	}

	public double getAvgRssi() {
		return avgRssi;
	}

	public void setAvgRssi(short avgRssi) {
		this.avgRssi = avgRssi;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(short channel) {
		this.channel = channel;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getLength() {
        return CoapConstants.MAC_ID_STRING_LEN + 12;
    }

    @Override
    public void readFrom(ChannelBuffer data) {
    	this.ap = StringByteSerializer.readFrom(data,
    			CoapConstants.MAC_ID_STRING_LEN);
    	this.timestamp = data.readInt();
    	this.avgRssi = data.readInt();
    	this.channel = data.readShort();
    	data.readByte(); // pad
    	data.readByte(); // pad
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.ap);
    	data.writeInt(this.timestamp);
    	data.writeInt(this.avgRssi);
    	data.writeShort(this.channel);
        data.writeByte((byte) 0); // pad
        data.writeByte((byte) 0); // pad
    }
    
	@Override
	public String toString() {
		return "OFBeaconStatisticsReply [ap=" + ap + ", timestamp=" + timestamp
				+ ", avgRssi=" + avgRssi + ", channel=" + channel + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 521;
        int result = 1;
        result = prime
                * result
                + ((this.ap == null) ? 0 : this.ap.hashCode());
        
        result = prime * result + this.channel;
        result = prime * result + (this.avgRssi ^ (this.avgRssi >>> 32));
        result = prime * result + (this.timestamp ^ (this.timestamp >>> 32));
        
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
        if (!(obj instanceof OFBeaconStatisticsReply)) {
            return false;
        }
        
        OFBeaconStatisticsReply other = (OFBeaconStatisticsReply) obj;
        
        if (!this.ap.equals(other.ap)) {
            return false;
        }

        if (this.channel != other.channel) {
        	return false;
        }
        
        if (this.avgRssi != other.avgRssi) {
        	return false;
        }
        
        if (this.timestamp != other.timestamp){
        	return false;
        }
        
        return true;
    }
}
