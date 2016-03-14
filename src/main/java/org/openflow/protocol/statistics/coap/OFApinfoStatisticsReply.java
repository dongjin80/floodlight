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
 * Represents an ofp_apinfo_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFApinfoStatisticsReply implements OFStatistics {
    protected String apMacId; /* Identifier for the AP */
    protected int timestamp; /* unix timestamp in seconds */
    protected byte nicCount; /* NIC number */
    
    protected short is2GhzSupported; /* 2 GHz support? */
    protected short is5GhzSupported; /* 2 GHz support? */
    
    protected short is2GhzHT20Supported; /* 2 GHz HT20 support? */
    protected short is2GhzHT40Supported; /* 2 GHz HT40 support? */
    protected short is5GhzHT20Supported; /* 2 GHz HT20 support? */
    protected short is5GhzHT40Supported; /* 2 GHz HT20 support? */
    
    public String getApMacId() {
		return apMacId;
	}

	public void setApMacId(String apMacId) {
		this.apMacId = apMacId;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public byte getNicCount() {
		return nicCount;
	}

	public void setNicCount(byte nicCount) {
		this.nicCount = nicCount;
	}

	public short getIs2GhzSupported() {
		return is2GhzSupported;
	}

	public void setIs2GhzSupported(short is2GhzSupported) {
		this.is2GhzSupported = is2GhzSupported;
	}

	public short getIs5GhzSupported() {
		return is5GhzSupported;
	}

	public void setIs5GhzSupported(short is5GhzSupported) {
		this.is5GhzSupported = is5GhzSupported;
	}

	public short getIs2GhzHT20Supported() {
		return is2GhzHT20Supported;
	}

	public void setIs2GhzHT20Supported(short is2GhzHT20Supported) {
		this.is2GhzHT20Supported = is2GhzHT20Supported;
	}

	public short getIs2GhzHT40Supported() {
		return is2GhzHT40Supported;
	}

	public void setIs2GhzHT40Supported(short is2GhzHT40Supported) {
		this.is2GhzHT40Supported = is2GhzHT40Supported;
	}

	public short getIs5GhzHT20Supported() {
		return is5GhzHT20Supported;
	}

	public void setIs5GhzHT20Supported(short is5GhzHT20Supported) {
		this.is5GhzHT20Supported = is5GhzHT20Supported;
	}

	public short getIs5GhzHT40Supported() {
		return is5GhzHT40Supported;
	}

	public void setIs5GhzHT40Supported(short is5GhzHT40Supported) {
		this.is5GhzHT40Supported = is5GhzHT40Supported;
	}
	
	@Override
	public int getLength() {
		return 32;
	}

	@Override
    public void readFrom(ChannelBuffer data) {
    	this.apMacId = StringByteSerializer.readFrom(data,
    			CoapConstants.MAC_ID_STRING_LEN);
    	this.timestamp = data.readInt();
    	
    	this.nicCount = data.readByte();
    	
    	this.is2GhzSupported = data.readByte();
    	this.is5GhzSupported = data.readByte();
    	
    	this.is2GhzHT20Supported = data.readByte();
    	this.is2GhzHT40Supported = data.readByte();
    	this.is5GhzHT20Supported = data.readByte();
    	this.is5GhzHT40Supported = data.readByte();
    	
    	data.readByte(); // pad
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.apMacId);
    	data.writeInt(this.timestamp);
    	
    	data.writeByte(this.nicCount);
    	
    	data.writeByte(this.is2GhzSupported);
        data.writeByte(this.is5GhzSupported);
        
        data.writeByte(this.is2GhzHT20Supported);
        data.writeByte(this.is2GhzHT40Supported);
        data.writeByte(this.is5GhzHT20Supported);
        data.writeByte(this.is5GhzHT40Supported);
        
        data.writeByte((byte) 0); // pad
    }
    
	@Override
	public String toString() {
		return "OFApinfoStatisticsReply [apMacId=" + apMacId + ", timestamp="
				+ timestamp + ", nicCount=" + nicCount + ", is2GhzSupported="
				+ is2GhzSupported + ", is5GhzSupported=" + is5GhzSupported
				+ ", is2GhzHT20Supported=" + is2GhzHT20Supported
				+ ", is2GhzHT40Supported=" + is2GhzHT40Supported
				+ ", is5GhzHT20Supported=" + is5GhzHT20Supported
				+ ", is5GhzHT40Supported=" + is5GhzHT40Supported + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 643;
        int result = 1;
        result = prime
                * result
                + ((this.apMacId == null) ? 0 : this.apMacId.hashCode());
        
        result = prime * result + this.nicCount;
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
        if (!(obj instanceof OFApinfoStatisticsReply)) {
            return false;
        }
        
        OFApinfoStatisticsReply other = (OFApinfoStatisticsReply) obj;
        
        if (!this.apMacId.equals(other.apMacId)) {
            return false;
        }

        if (this.nicCount != other.nicCount) {
        	return false;
        }
        
        if (this.timestamp != other.timestamp){
        	return false;
        }
        
        return true;
    }
}
