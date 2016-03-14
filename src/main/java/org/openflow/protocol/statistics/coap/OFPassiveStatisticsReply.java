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
 * Represents an ofp_passive_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFPassiveStatisticsReply implements OFStatistics {

    protected String sender;
    protected String receiver;
    protected String retryString;
    
    protected short averagePacketLength;
    protected short averageRate;
    
    protected int rssi;
    protected int packetCount;
    protected int packetRetries;
    protected int channel;
    protected int timestamp;
        
	// Debug/Experiment related variables.
    protected short type;
	
    public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getRetryString() {
		return retryString;
	}

	public void setRetryString(String retruyString) {
		this.retryString = retruyString;
	}

	public short getAveragePacketLength() {
		return averagePacketLength;
	}

	public void setAveragePacketLength(short averagePacketLength) {
		this.averagePacketLength = averagePacketLength;
	}

	public short getAverageRate() {
		return averageRate;
	}

	public void setAverageRate(short averageRate) {
		this.averageRate = averageRate;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
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

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	@Override
    public int getLength() {
        return 228; // CoapConstants.STATION_STATS_LENGTH * 3;
    }
    
    @Override
    public void readFrom(ChannelBuffer data) {
    	this.sender = StringByteSerializer.readFrom(data,
    			CoapConstants.MAC_ID_STRING_LEN);
    	
    	this.receiver = StringByteSerializer.readFrom(data,
    			CoapConstants.MAC_ID_STRING_LEN);
    	
    	this.retryString = StringByteSerializer.readFrom(data,
    			CoapConstants.RATE_STRING_LENGTH);

    	this.averagePacketLength = data.readShort();
    	this.averageRate = data.readShort();
    	
    	this.rssi = data.readInt();
    	this.packetCount = data.readInt();
    	this.packetRetries = data.readInt();
    	this.channel = data.readInt();
    	this.timestamp = data.readInt();
    	
    	this.type = data.readShort();
    	
    	data.readByte(); // pad
    	data.readByte(); // pad
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.sender);

    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.receiver);

    	StringByteSerializer.writeTo(data, CoapConstants.MAC_ID_STRING_LEN,
                this.retryString);
    	
    	data.writeShort(this.averagePacketLength);
    	data.writeShort(this.averageRate);
    	
    	data.writeInt(this.rssi);
    	data.writeInt(this.packetCount);
    	data.writeInt(this.packetRetries);
    	data.writeInt(this.channel);
    	data.writeInt(this.timestamp);
    	
    	data.writeShort(this.type);
        data.writeByte((byte) 0); // pad
        data.writeByte((byte) 0); // pad
    }

    @Override
	public String toString() {
		return "OFPassiveStatisticsReply [sender=" + sender + ", receiver="
				+ receiver + ", retryString=" + retryString
				+ ", averagePacketLength=" + averagePacketLength
				+ ", averageRate=" + averageRate + ", rssi=" + rssi
				+ ", packetCount=" + packetCount + ", packetRetries="
				+ packetRetries + ", channel=" + channel + ", timestamp="
				+ timestamp + ", type=" + type + "]";
	}

	@Override
    public int hashCode() {
        final int prime = 617;
        int result = 1;
        
        result = prime
                * result
                + ((sender == null) ? 0 : sender
                        .hashCode());

        result = prime
                * result
                + ((receiver == null) ? 0 : receiver
                        .hashCode());
        
        result = prime
                * result
                + ((retryString == null) ? 0 : retryString
                        .hashCode());
        
        result = prime * result + this.packetCount;
        result = prime * result + (this.packetRetries ^ (this.packetRetries >>> 32));
        result = prime * result + (this.timestamp ^ (this.timestamp >>> 32));
        result = prime * result + (this.channel ^ (this.channel >>> 32));
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
        if (!(obj instanceof OFPassiveStatisticsReply)) {
            return false;
        }
        
        OFPassiveStatisticsReply other = (OFPassiveStatisticsReply) obj;
        
        if (!this.sender.equals(other.sender)) {
            return false;
        }
        
        if (!this.receiver.equals(other.receiver)) {
            return false;
        }
        
        if (!this.retryString.equals(other.retryString)) {
            return false;
        }

        if (this.channel != other.channel) {
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
