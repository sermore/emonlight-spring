package net.reliqs.emonlight.xbeegw.xbee;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.Instant;

class VCCProcessor extends MessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(VCCProcessor.class);

    VCCProcessor(Processor processor) {
        super(processor);
    }

    @Override
    void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
        Node node = ns.getNode();
        Probe p = node.findProbeByTypeAndPort(Type.VCC, (byte) 0);
        Instant time;
        if (selector == (byte) 'V') {
            time = m.getTime();
        } else {
            int t = in.getInt();
            time = calculateTimeFromDataMessage(ns, t);
        }
        short v = in.getShort();
        double vcc = node.isVccFromADC() ? node.getAdcVRef() / node.getAdcRange() * v * p.getAdcMult()
                : 0.0 + v / 1000.0;
        log.info("{}: Vcc = {} @{}", node, vcc, time);
        verifyTime(node, time);
        Data data = new Data(time.toEpochMilli(), vcc);
        if (vccInRange(vcc)) {
            publish(p, Type.VCC, data);
        } else {
            log.warn("{}: Vcc {} discarded as out of range", node, vcc);
        }
    }

    private boolean vccInRange(double vcc) {
        return vcc > 0 && vcc < 24D;
    }

    //	@Override
//	public void triggerChanged(NodeState ns, Probe p, int oldState, int newState) {
//		// TODO implements Voltage trigger
//	}

}
