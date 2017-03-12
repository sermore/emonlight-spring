package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Instant;

import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

class VCCProcessor extends MessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(VCCProcessor.class);

	VCCProcessor(Processor processor) {
		super(processor);
	}

	@Override
	void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
		Node node = ns.getNode();
		Probe p = node.getProbe(Type.VCC, (byte) 0);
		Instant time;
		if (selector == (byte) 'V') {
			time = m.time;
		} else {
			int t = in.getInt();
			time = calculateTimeFromDataMessage(ns, t);
		}
		short v = in.getShort();
		double vcc = node.isVccFromADC() ? node.getAdcVRef() / node.getAdcRange() * v * p.getAdcMult()
				: 0.0 + v / 1000.0;
		log.debug("{}: Vcc = {} @{}", node, vcc, time);
		verifyTime(node, time);
        Data data = new Data(time.toEpochMilli(), vcc);
        publish(p, Type.VCC, data);
	}

//	@Override
//	public void triggerChanged(NodeState ns, Probe p, int oldState, int newState) {
//		// TODO implements Voltage trigger
//	}

}
