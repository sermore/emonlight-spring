package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;

class DS18B20Processor extends MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(DS18B20Processor.class);

    DS18B20Processor(Processor processor) {
        super(processor);
    }

    @Override
    void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
        Node node = ns.getNode();

        Instant time;
        if (selector == (byte) 'S') {
            time = m.getTime();
        } else {
            int tt = in.getInt();
            time = calculateTimeFromDataMessage(ns, tt);
        }
        short d = in.getShort();
        double t = (int) (d / 100) + (d % 100) / 100.0;
        log.info("{}: DS18B20 T={}, D={}, @{}", node, t, d, time);
        verifyTime(node, time);
        Data data = new Data(time.toEpochMilli(), t);
        if (temperatureInRange(t)) {
            publish(node.findProbeByType(Type.DS18B20), Type.DS18B20, data);
        } else {
            log.warn("{}: DS18B20 T {} discarded as out of range", node, t);
        }
    }

    private boolean temperatureInRange(double t) {
        return t >= -60 && t <= 130;
    }

}
