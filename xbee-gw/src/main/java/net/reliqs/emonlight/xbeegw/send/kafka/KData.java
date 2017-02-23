package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.xbeegw.xbee.Data;

class KData {

	final String server;
	final String apiKey;
	final Data data;

	KData(String server, String apiKey, Data data) {
		super();
		this.server = server;
		this.apiKey = apiKey;
		this.data = data;
	}

}