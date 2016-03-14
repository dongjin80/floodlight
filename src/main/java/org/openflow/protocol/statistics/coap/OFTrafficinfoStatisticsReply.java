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
 * Represents an ofp_trafficinfo_stats_reply structure
 * @author Ashish Patro (patro@cs.wisc.edu)
 */
public class OFTrafficinfoStatisticsReply implements OFStatistics {
    protected String trafficInfoStatsString;
    
    public String getTrafficInfoStatsString() {
		return trafficInfoStatsString;
	}

	public void setTrafficInfoStatsString(String syncBeaconTsString) {
		this.trafficInfoStatsString = syncBeaconTsString;
	}

    public int getLength() {
        return CoapConstants.TRAFFICINFO_STATS_LENGTH;
    }

    @Override
    public void readFrom(ChannelBuffer data) {
    	this.trafficInfoStatsString = StringByteSerializer.readFrom(data,
    			CoapConstants.TRAFFICINFO_STATS_LENGTH);
    }

    @Override
    public void writeTo(ChannelBuffer data) {
    	StringByteSerializer.writeTo(data, CoapConstants.TRAFFICINFO_STATS_LENGTH,
                this.trafficInfoStatsString);
    }

    @Override
    public int hashCode() {
        final int prime = 599;
        int result = 1;
        result = prime
                * result
                + ((trafficInfoStatsString == null) ? 0 : trafficInfoStatsString
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
        if (!(obj instanceof OFTrafficinfoStatisticsReply)) {
            return false;
        }
        
        OFTrafficinfoStatisticsReply other = (OFTrafficinfoStatisticsReply) obj;
        
        if (trafficInfoStatsString == null) {
            if (other.trafficInfoStatsString != null) {
                return false;
            }
        }
        
        return true;
    }
}
