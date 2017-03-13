package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Node;

class MultiDataProcessor extends MessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(MultiDataProcessor.class);

	MultiDataProcessor(Processor processor) {
		super(processor);
	}

	@Override
	void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
		Node node = ns.getNode();
		int timeMSec = in.getInt();
		long dt = Integer.toUnsignedLong(timeMSec - ns.lastTimeMSec);
		long dtFromTime = Duration.between(ns.lastTime, m.getTime()).toMillis();
		long d = dt - dtFromTime;
		if (Math.abs(d) > 5000) {
			ns.skipLastTime = true;
			// } else {
			// node.lastTime = m.time.plus(d, ChronoUnit.MILLIS);
		}
		ns.dataTime = m.getTime();
		ns.dataTimeMSec = timeMSec;
		ns.delta = d;
		log.debug("{}: Data T={}, DT={}, D={}, @{} skipNext={}", node, timeMSec, dt, d, m.getTime(), ns.skipLastTime);
	}

}
