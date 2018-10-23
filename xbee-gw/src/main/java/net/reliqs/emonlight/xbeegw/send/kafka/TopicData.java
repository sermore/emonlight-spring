package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.data.Data;

public class TopicData {
    public String topic;
    public Data data;

    public TopicData(String topic, Data data) {
        this.topic = topic;
        this.data = data;
    }

    public static String getTopic(int gatewayId, Probe p) {
        return p.getNode().getName() + "_" + p.getName();
    }
}
