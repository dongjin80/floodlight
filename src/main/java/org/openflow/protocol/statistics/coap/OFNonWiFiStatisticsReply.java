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


import org.jboss.netty.buffer.ChannelBuffer;
import org.openflow.protocol.statistics.OFStatistics;

/**
 * Represents an ofp_nonwifi_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFNonWiFiStatisticsReply implements OFStatistics {
	protected int timestamp;
	protected int startTs;
	protected int endTs;

	protected int duration;
	protected int rssi;

	protected short startBin;
	protected short centerBin;
	protected short endBin;

	protected short deviceType;

	protected int channel;
	
	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public int getStartTs() {
		return startTs;
	}

	public void setStartTs(int startTs) {
		this.startTs = startTs;
	}

	public int getEndTs() {
		return endTs;
	}

	public void setEndTs(int endTs) {
		this.endTs = endTs;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public short getStartBin() {
		return startBin;
	}

	public void setStartBin(short startBin) {
		this.startBin = startBin;
	}

	public short getCenterBin() {
		return centerBin;
	}

	public void setCenterBin(short centerBin) {
		this.centerBin = centerBin;
	}

	public short getEndBin() {
		return endBin;
	}

	public void setEndBin(short endBin) {
		this.endBin = endBin;
	}

	public short getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(short deviceType) {
		this.deviceType = deviceType;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	@Override
	public int getLength() {
		return 32;
	}

    @Override
    public void readFrom(ChannelBuffer data) {
    	this.timestamp = data.readInt();
    	this.startTs = data.readInt();
    	this.endTs = data.readInt();

    	this.duration = data.readInt();
    	this.rssi = data.readInt();

    	this.startBin = data.readShort();
    	this.centerBin = data.readShort();
    	this.endBin = data.readShort();

    	this.deviceType = data.readShort();

    	this.channel = data.readInt();
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	data.writeInt(this.timestamp);
    	data.writeInt(this.startTs);
    	data.writeInt(this.endTs);
    	
    	data.writeInt(this.duration);
    	data.writeInt(this.rssi);
    	
    	data.writeShort(this.startBin);
    	data.writeShort(this.centerBin);
    	data.writeShort(this.endBin);
    	
    	data.writeShort(this.deviceType);
    	
    	data.writeInt(this.channel);
    }

    @Override
	public String toString() {
		return "OFNonWiFiStatisticsReply [timestamp=" + timestamp
				+ ", startTs=" + startTs + ", endTs=" + endTs + ", duration="
				+ duration + ", rssi=" + rssi + ", startBin=" + startBin
				+ ", centerBin=" + centerBin + ", endBin=" + endBin
				+ ", deviceType=" + deviceType + ", channel=" + channel + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 563;
        int result = 1;
        
        result = prime * result + this.timestamp;
        result = prime * result + (this.startTs ^ (this.startTs >>> 32));
        result = prime * result + (this.endTs ^ (this.endTs >>> 32));
        result = prime * result + (this.duration ^ (this.duration >>> 32));
        result = prime * result + (this.rssi ^ (this.rssi >>> 32));
        result = prime * result + (this.startBin ^ (this.startBin >>> 32));
        result = prime * result + (this.centerBin ^ (this.centerBin >>> 32));
        result = prime * result + (this.endBin ^ (this.endBin >>> 32));
        result = prime * result + (this.deviceType ^ (this.deviceType >>> 32));
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
        if (!(obj instanceof OFNonWiFiStatisticsReply)) {
            return false;
        }
        
        OFNonWiFiStatisticsReply other = (OFNonWiFiStatisticsReply) obj;
        
        if (this.timestamp != other.timestamp) {
        	return false;
        }
        
        if (this.startTs != other.startTs) {
        	return false;
        }
        
        if (this.endTs != other.endTs) {
        	return false;
        }
        
        if (this.duration != other.duration) {
        	return false;
        }
        
        if (this.rssi != other.rssi) {
        	return false;
        }
        
        if (this.startBin != other.startBin) {
        	return false;
        }
        
        if (this.centerBin != other.centerBin) {
        	return false;
        }
        
        if (this.endBin != other.endBin) {
        	return false;
        }
        
        if (this.deviceType != other.deviceType) {
        	return false;
        }
        
        if (this.channel != other.channel) {
        	return false;
        }
        
        return true;
    }
}
