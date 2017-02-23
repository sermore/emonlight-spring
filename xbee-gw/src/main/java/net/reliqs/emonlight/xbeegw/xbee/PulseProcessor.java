package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;

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
        Probe probe = ns.getProbe(Type.PULSE);
        double pow = ns.skipLastTime ? 0 : calcPower(probe.getPulsesPerKilowattHour(), dt);
        log.debug("{}: Pulse({}) Pow={}, T={}, DT={} @{}, skipped={}", node, port, pow, Integer.toUnsignedLong(t), dt,
                time, ns.skipLastTime);
        verifyTime(node, time);
        ProbeData probeData = ns.getProbeData(Type.PULSE);
        probeData.add(this, new Data(time.toEpochMilli(), pow));
        ns.lastTimeMSec = t;
        ns.lastTime = time;
        ns.skipLastTime = false;
    }

    @Override
    public void trigger(NodeState ns, Probe p, Type type, boolean enable) {
        int level = calcBuzzerLevel(type, enable);
        log.warn("{}: {} {} {} => Buzzer level {}", p.getNode(), p.getName(), type, enable, level);
        sendBuzzerAlarmLevel(ns, level);
    }

    int calcBuzzerLevel(Type type, boolean enable) {
        int level = enable ? 4 - type.ordinal() + Type.M_HARD_THRESHOLD.ordinal() : 0;
        return level;
    }

    double calcPower(int pulsesPerKilowattHour, long dt) {
        return dt > 0 ? (3600_000_000.0 / dt) / pulsesPerKilowattHour : 0;
    }

}
