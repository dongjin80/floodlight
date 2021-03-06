package net.floodlightcontroller.core.coap.structs;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

/**
 * Identifiers for different non-WiFi devices.
 *  
 * @author "Ashish Patro"
 *
 */
public enum NonWiFiDeviceType {
	PULSE_NONE(100),                                                                     
	PULSE_FHSSPHONE_LOW_CANDIDATE(101),
	PULSE_XBOX_CANDIDATE (102),                                                           
	PULSE_XBOX (103),                                                                     
	PULSE_FH_DEVICE (104), 
	PULSE_FHSSPHONE_LOW (105),
	PULSE_FHSSPHONE_HIGH (106),                                                           
	PULSE_BLUETOOTH (107),                                                                
	PULSE_BLUETOOTH_SCO (108),                                                            
	PULSE_MWO (110),
	PULSE_DISCARD (112),
	PULSE_UNK_FH (113), 

	PULSE_ANALOGPHONE (1),                                                                
	PULSE_ZIGBEE (2),
	PULSE_VIDEOCAM (3),                                                                   
	PULSE_UNK_HIGHDUTY (4), 
	
	NOT_AVAILABLE(0);

	private int value;

	private NonWiFiDeviceType(int value) {
		this.value = value;
	}

	private static final Map<Integer, NonWiFiDeviceType> lookup 	= new HashMap<Integer, NonWiFiDeviceType>();

	static {
		for (NonWiFiDeviceType s : EnumSet.allOf(NonWiFiDeviceType.class))
			lookup.put(s.getValue(), s);
	}

	public int getValue() {
		return value; 
	}

	public static NonWiFiDeviceType get(int code) { 
		return lookup.get(code); 
	}
}