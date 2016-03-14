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
 * Represents an ofp_util_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFUtilStatisticsReply implements OFStatistics {
	protected short type;        /* util or util hop */
	protected short utilVal;          /* Utilization value */
	protected int active;       /* active period in seconds */
	protected int busy;       /* busy period in seconds */
	protected int receive;     /* receive period in seconds */
	protected int xmit;      /* transmit period in seconds */
	protected int channel;      /* transmit period in seconds */
	protected int timestamp; /* unix timestamp in seconds */
	protected int noiseFloor; /* the noise floor in -dBm */

	// Debug/Experiment related variables.
    protected String utilhopStatsString;
	
    public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getUtilVal() {
		return utilVal;
	}

	public void setUtilVal(short utilVal) {
		this.utilVal = utilVal;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public int getBusy() {
		return busy;
	}

	public void setBusy(int busy) {
		this.busy = busy;
	}

	public int getReceive() {
		return receive;
	}

	public void setReceive(int receive) {
		this.receive = receive;
	}

	public int getXmit() {
		return xmit;
	}

	public void setXmit(int xmit) {
		this.xmit = xmit;
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

	public int getNoiseFloor() {
		return noiseFloor;
	}

	public void setNoiseFloor(int noiseFloor) {
		this.noiseFloor = noiseFloor;
	}
	
	public String getUtilhopStatsString() {
		return utilhopStatsString;
	}

	public void setUtilhopStatsString(String utilhopStatsString) {
		this.utilhopStatsString = utilhopStatsString;
	}

    @Override
    public int getLength() {
        return 32 + CoapConstants.UTILHOP_STATS_LENGTH;
    }
    
    @Override
	public String toString() {
		return "util;" + this.utilVal + " " + this.active + " " + this.busy + " " + this.receive + " " + this.xmit + " " + 
			this.channel + " " + this.timestamp + " " + this.noiseFloor;
	}

    @Override
    public void readFrom(ChannelBuffer data) {
    	this.type = data.readShort();        /* util or util hop */
    	this.utilVal = data.readShort();           /* Utilization value */
    	this.active = data.readInt();       /* active period in seconds */
    	this.busy = data.readInt();       /* busy period in seconds */
    	this.receive = data.readInt();     /* receive period in seconds */
    	this.xmit = data.readInt();      /* transmit period in seconds */
    	this.channel = data.readInt();      /* transmit period in seconds */
    	this.timestamp = data.readInt(); /* unix timestamp in seconds */
    	this.noiseFloor = data.readInt(); /* the noise floor in -dBm */
    	
    	// Debug related.
    	this.utilhopStatsString = StringByteSerializer.readFrom(data,
    			CoapConstants.UTILHOP_STATS_LENGTH);
    }

    @Override
    public void writeTo(ChannelBuffer data) {
        data.writeShort(this.type);
        data.writeShort(this.utilVal);
        data.writeInt(this.active);
        data.writeInt(this.busy);
        data.writeInt(this.receive);
        data.writeInt(this.xmit);
        data.writeInt(this.channel);
        data.writeInt(this.timestamp);
        data.writeInt(this.noiseFloor);
        
        // Debug related.
    	StringByteSerializer.writeTo(data, CoapConstants.UTILHOP_STATS_LENGTH,
                this.utilhopStatsString);
    }

    @Override
    public int hashCode() {
        final int prime = 607;
        int result = 1;
        result = prime * result + (utilVal ^ (utilVal >>> 16));
        result = prime * result + type;
        result = prime * result + (active ^ (active >>> 32));
        result = prime * result + (busy ^ (busy >>> 32));
        result = prime * result + (receive ^ (receive >>> 32));
        result = prime * result + (xmit ^ (xmit >>> 32));
        result = prime * result + (channel ^ (channel >>> 32));
        result = prime * result + (timestamp ^ (timestamp >>> 32));
        result = prime * result + (noiseFloor ^ (noiseFloor >>> 32));
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
        if (!(obj instanceof OFUtilStatisticsReply)) {
            return false;
        }
        OFUtilStatisticsReply other = (OFUtilStatisticsReply) obj;
        if (type != other.type) {
            return false;
        }
        if (utilVal != other.utilVal) {
            return false;
        }
        if (active != other.active) {
            return false;
        }
        if (busy != other.busy) {
            return false;
        }
        if (receive != other.receive) {
            return false;
        }
        if (xmit != other.xmit) {
            return false;
        }
        if (channel != other.channel) {
            return false;
        }
        if (timestamp != other.timestamp) {
            return false;
        }
        if (noiseFloor != other.noiseFloor) {
            return false;
        }
        return true;
    }
}
