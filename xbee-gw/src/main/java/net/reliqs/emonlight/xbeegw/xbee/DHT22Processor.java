package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;

class DHT22Processor extends MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(DHT22Processor.class);

    DHT22Processor(Processor processor) {
        super(processor);
    }

    @Override
    void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
        Node node = ns.getNode();
        DHT22Data d = new DHT22Data(in);
        if (d.check()) {
            Instant time;
            if (selector == (byte) 'J') {
                time = m.getTime();
            } else {
                int tt = in.getInt();
                time = calculateTimeFromDataMessage(ns, tt);
            }
            double hv = d.humidity();
            double tv = d.temperature();
            log.info("{}: DHT22 P={}, T={}, H={} @{}", node, d.port, tv, hv, time);
            verifyTime(node, time);
            Data dataH = new Data(time.toEpochMilli(), hv);
            Data dataT = new Data(time.toEpochMilli(), tv);
            if (temperatureInRange(tv)) {
                publish(node.findProbeByTypeAndPort(Type.DHT22_H, d.port), Type.DHT22_H, dataH);
            } else {
                log.warn("{}: DHT22 T {} discarded as out of range", node, tv);
            }
            if (humidityInRange(hv)) {
                publish(node.findProbeByTypeAndPort(Type.DHT22_T, d.port), Type.DHT22_T, dataT);
            } else {
                log.warn("{}: DHT22 H {} discarded as out of range", node, hv);
            }
        } else {
            log.warn("{}: error reading DHT22 data", node);
        }
    }

    private boolean humidityInRange(double hv) {
        return hv >= 0 && hv <= 100;
    }

    private boolean temperatureInRange(double tv) {
        return tv >= -50 && tv <= 90;
    }

}
