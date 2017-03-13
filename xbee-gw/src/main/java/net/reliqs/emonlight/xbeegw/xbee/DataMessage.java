package net.reliqs.emonlight.xbeegw.xbee;

import java.time.Instant;

import com.digi.xbee.api.models.XBeeMessage;

class DataMessage {
	final private XBeeMessage msg;
	final private Instant time;

	DataMessage(XBeeMessage msg) {
		super();
        this.msg = msg;
		this.time = Instant.now();
	}
	
	String getDeviceAddress() {
        return msg != null ? msg.getDevice().get64BitAddress().toString() : null;
	}
	
	byte[] getData() {
	    return msg.getData();
	}

    Instant getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "DataMessage [msg=" + msg + ", time=" + time + "]";
    }

}
