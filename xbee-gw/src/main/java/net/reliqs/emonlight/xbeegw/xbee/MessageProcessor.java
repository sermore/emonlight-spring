package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.utils.ByteUtils;
import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerHandler;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerLevel;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

abstract class MessageProcessor implements TriggerHandler {
//	private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    private final Processor processor;
    private Map<Probe, TriggerLevel> triggers;


    MessageProcessor(Processor processor) {
        super();
        this.processor = processor;
        this.triggers = new HashMap<>();
    }

    abstract void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in);

    /**
     * Trigger called when a threshold is trespassed. The parameters type and
     * enable define the new state of the alarm level that needs to be set.
     */
    @Override
    public void triggerChanged(NodeState ns, Probe p, Type type, int oldState, int newState) {
        publish(p, type, new Data(Instant.now().toEpochMilli(), newState));
    }

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
        assert now.isAfter(time) : "received message with timestamp ahead of now";
        assert now.minus(node.getSampleTime() * 2, ChronoUnit.MILLIS).isBefore(time) : String
                .format("received message timestamp %s is too old compared to now %s", time, now);
    }

    void sendBuzzerAlarmLevel(NodeState ns, int level) {
        ByteBuffer b = ByteBuffer.allocate(3);
        b.put((byte) 'S');
        b.put((byte) 'B');
        b.put((byte) level);
        processor.sendData(ns, b.array());
    }

    void publish(Probe probe, Type type, Data data) {
        if (type == probe.getType()) {
            TriggerLevel t = triggers.get(probe);
            if (t != null) {
//                log.trace("{}: process trigger {}", probe.getNode(), type);
                t.process(data);
            }
        }
        processor.publish(probe, type, data);
    }

    void registerTrigger(NodeState ns, Probe p) {
        TriggerLevel tl = new TriggerLevel(p, ns, TriggerLevel.powerTriggers(p));
        tl.addHandler(this);
        triggers.put(p, tl);
    }
}
