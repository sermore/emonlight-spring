package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.utils.ByteUtils;
import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

abstract class MessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    private final Processor processor;

    MessageProcessor(Processor processor) {
        super();
        this.processor = processor;
    }

    abstract void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in);

    void sendOK(NodeState ns) {
        byte[] b = ByteUtils.stringToByteArray("OK");
        processor.sendData(ns, b);
    }

    void sendDeviceConfiguration(NodeState ns, DeviceConfig storedCfg) {
        processor.sendData(ns, storedCfg.buildResponse());
    }

    Instant calculateTimeFromDataMessage(NodeState ns, int t) {
        long dt = Integer.toUnsignedLong(ns.dataTimeMSec - t);
        Instant time = ns.dataTime.minus(dt, ChronoUnit.MILLIS);
        return time;
    }

    void verifyTime(Node node, Instant time) {
        Instant now = Instant.now().plus(1, ChronoUnit.SECONDS);
        if (now.isBefore(time)) {
            log.warn("{}: {} received message with timestamp ahead of now", node, time);
        }
        if (now.minus(node.getSampleTime() * 2, ChronoUnit.MILLIS).isAfter(time)) {
            log.warn("{}: message time {} too old compared to now {}", node, time, now);
        }
    }

    void sendBuzzerAlarmLevel(NodeState ns, int level) {
        ByteBuffer b = ByteBuffer.allocate(3);
        b.put((byte) 'S');
        b.put((byte) 'B');
        b.put((byte) level);
        processor.sendData(ns, b.array());
    }

    void publish(Probe probe, Type type, Data data) {
        processor.publish(probe, type, data);
    }
}
