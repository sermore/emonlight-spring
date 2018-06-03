package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

class PulseProcessor extends MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(PulseProcessor.class);
    private GlobalState globalState;

    PulseProcessor(Processor processor, GlobalState globalState) {
        super(processor);
        this.globalState = globalState;
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
        Probe probe = node.findProbeByTypeAndPort(Type.PULSE, port);
        double pow = ns.skipLastTime ? 0 : calcPower(probe.getPulsesPerKilowattHour(), dt);
        log.info("{}: Pulse({}) Pow={}, T={}, DT={} @{}, skipped={}", node, port, pow, Integer.toUnsignedLong(t), dt,
                time, ns.skipLastTime);
        verifyTime(node, time);
        Data data = new Data(time.toEpochMilli(), pow);
        if (!ns.skipLastTime) {
            if (powerInRange(pow, probe.getPulsesPerKilowattHour())) {
                publish(probe, Type.PULSE, data);
            } else {
                log.warn("{}: Pulse({}) Power {} discarded as out of range", node, port, pow);
            }
        }
        ns.lastTimeMSec = t;
        ns.lastTime = time;
        ns.skipLastTime = false;
    }

    private boolean powerInRange(double pow, Integer pulsesPerKilowattHour) {
        return pow > 0 && pow < calcPower(pulsesPerKilowattHour, 200L);
    }

    double calcPower(int pulsesPerKilowattHour, long dt) {
        return dt > 0 ? (3600_000_000.0 / dt) / pulsesPerKilowattHour : 0;
    }

}
