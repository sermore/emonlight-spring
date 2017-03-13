package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Instant;

import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

class DHT22Processor extends MessageProcessor {
	private static final Logger log = LoggerFactory.getLogger(DHT22Processor.class);

	DHT22Processor(Processor processor) {
		super(processor);
	}

	@Override
	void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in) {
		Node node = ns.getNode();
		DHT22Data d = new DHT22Data(in);
		if (d.check()) {
			Instant time;
			if (selector == (byte) 'J') {
				time = m.getTime();
			} else {
				int tt = in.getInt();
				time = calculateTimeFromDataMessage(ns, tt);
			}
			double hv = d.humidity();
			double tv = d.temperature();
			log.debug("{}: DHT22 P={}, T={}, H={} @{}", node, d.port, tv, hv, time);
			verifyTime(node, time);
            Data dataH = new Data(time.toEpochMilli(), hv);
            Data dataT = new Data(time.toEpochMilli(), tv);
			publish(node.getProbe(Type.DHT22_H, d.port), Type.DHT22_H, dataH);
            publish(node.getProbe(Type.DHT22_T, d.port), Type.DHT22_T, dataT);
		} else {
			log.warn("{}: error reading DHT22 data", node);
		}
	}

}
