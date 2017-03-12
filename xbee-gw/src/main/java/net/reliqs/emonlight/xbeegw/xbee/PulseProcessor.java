package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

class PulseProcessor extends MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(PulseProcessor.class);

    PulseProcessor(Processor processor) {
        super(processor);
    }

    @Override
    void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
        byte port = in.get();
        int t = in.getInt();
        long dt;
        Instant time;
        Node node = ns.getNode();
        if (ns.skipLastTime) {
            dt = Integer.toUnsignedLong(ns.dataTimeMSec - t);
            time = ns.dataTime.minus(dt, ChronoUnit.MILLIS);
        } else {
            dt = Integer.toUnsignedLong(t - ns.lastTimeMSec);
            // adapt time in order to keep up with server time
            time = ns.lastTime.plus(dt + (ns.delta > 20 ? -2 : ns.delta < -20 ? 2 : 0),
                    ChronoUnit.MILLIS);
        }
        Probe probe = node.getProbe(Type.PULSE, port);
        double pow = ns.skipLastTime ? 0 : calcPower(probe.getPulsesPerKilowattHour(), dt);
        log.debug("{}: Pulse({}) Pow={}, T={}, DT={} @{}, skipped={}", node, port, pow, Integer.toUnsignedLong(t), dt,
                time, ns.skipLastTime);
        verifyTime(node, time);
        Data data = new Data(time.toEpochMilli(), pow);
        if (!ns.skipLastTime) {
            publish(probe, Type.PULSE, data);
        }
        ns.lastTimeMSec = t;
        ns.lastTime = time;
        ns.skipLastTime = false;
    }

    @Override
    public void triggerChanged(NodeState ns, Probe p, Type type, int oldState, int newState) {
        log.warn("{}: {} Buzzer level {} => {}", p.getNode(), p.getName(), oldState, newState);
        super.triggerChanged(ns, p, Type.THRESOLD_ALARM, oldState, newState);
        sendBuzzerAlarmLevel(ns, newState);
    }

    double calcPower(int pulsesPerKilowattHour, long dt) {
        return dt > 0 ? (3600_000_000.0 / dt) / pulsesPerKilowattHour : 0;
    }

}
