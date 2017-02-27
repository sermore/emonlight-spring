package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.Data;

public class TopicData {

	public static String getTopic(long gatewayId, Probe p) {
		return gatewayId + "_" + p.getName();
	}

	public final String topic;
	public final Data data;

	public TopicData(final String topic, Data data) {
		super();
		this.topic = topic;
		this.data = data;
	}

}