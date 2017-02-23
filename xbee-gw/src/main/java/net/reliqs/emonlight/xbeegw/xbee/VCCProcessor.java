package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;

class VCCProcessor extends MessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(VCCProcessor.class);

	VCCProcessor(Processor processor) {
		super(processor);
	}

	@Override
	void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
		Node node = ns.getNode();
		ProbeData pd = ns.getProbeData(Type.VCC);
		Probe p = ns.getProbe(Type.VCC);
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
		// TODO handle the possibility to not store the history
		pd.add(this, new Data(time.toEpochMilli(), vcc));
	}

	@Override
	public void trigger(NodeState ns, Probe p, Type type, boolean enable) {
	}

}
