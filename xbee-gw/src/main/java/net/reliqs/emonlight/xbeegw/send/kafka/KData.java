package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.xbeegw.xbee.Data;

class KData {

	final String topic;
	final Data data;

	KData(final String topic, Data data) {
		super();
		this.topic = topic;
		this.data = data;
	}

}