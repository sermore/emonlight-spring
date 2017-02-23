package net.reliqs.emonlight.xbeegw.xbee;

import java.time.Instant;

import com.digi.xbee.api.models.XBeeMessage;

class DataMessage {
	final XBeeMessage msg;
	final Instant time;

	public DataMessage(XBeeMessage msg) {
		super();
		this.msg = msg;
		this.time = Instant.now();
	}

}
