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
 * Represents an ofp_client_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFClientStatisticsReply implements OFStatistics {
    protected String apMacId; /* MAC address of the AP */
    protected String apHostname; /* Hostname of the current AP. */
    protected int timestamp; /* unix timestamp in seconds */
    protected int apIp; /* Local IP of the client */
    protected int devType; /*Information about the client device Type */
    
    public int getDevType() {
		return devType;
	}

	public void setDevType(int devType) {
		this.devType = devType;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getApIp() {
		return apIp;
	}

	public void setApIp(int apIp) {
		this.apIp = apIp;
	}

	public String getApMacId() {
		return apMacId;
	}

	public void setApMacId(String apMacId) {
		this.apMacId = apMacId;
	}

	public String getApHostname() {
		return apHostname;
	}

	public void setApHostname(String apHostname) {
		this.apHostname = apHostname;
	}

	public int getLength() {
        return 68; //CoapConstants.STATION_STATS_LENGTH;
    }

    @Override
    public void readFrom(ChannelBuffer data) {
    	this.apMacId = StringByteSerializer.readFrom(data,
    			CoapConstants.MAC_ID_STRING_LEN);
    	this.apHostname = StringByteSerializer.readFrom(data,
    			CoapConstants.HOSTNAME_LENGTH);
    	
    	this.timestamp = data.readInt();
    	this.apIp = data.readInt();
    	this.devType = data.readInt();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.apMacId);
    	StringByteSerializer.writeTo(data, CoapConstants.HOSTNAME_LENGTH,
                this.apHostname);
    	
    	data.writeInt(this.timestamp);
    	data.writeInt(this.apIp);
    	data.writeInt(this.devType);
    }

	@Override
	public String toString() {
		return "OFClientStatisticsReply [apMacId=" + apMacId + ", apHostname="
				+ apHostname + ", timestamp=" + timestamp + ", apIp=" + apIp
				+ ", devType=" + devType + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 541;
        int result = 1;
        
        result = prime * result + (this.timestamp ^ (this.timestamp >>> 32));
        result = (int) (prime * result + (this.apIp ^ (this.apIp >>> 32)));
        
        result = prime
                * result
                + ((apMacId == null) ? 0 : apMacId
                        .hashCode());
        
        result = prime
                * result
                + ((apHostname == null) ? 0 : apHostname
                        .hashCode());
        
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
        if (!(obj instanceof OFClientStatisticsReply)) {
            return false;
        }
        
        OFClientStatisticsReply other = (OFClientStatisticsReply) obj;
        
        if (!this.apMacId.equals(other.apMacId)) {
            return false;
        }
        
        if (!this.apHostname.equals(other.apHostname)) {
            return false;
        }
        
        if (this.apIp != other.apIp) {
        	return false;
        }
        
        if (this.timestamp != other.timestamp) {
        	return false;
        }
        
        return true;
    }
}
