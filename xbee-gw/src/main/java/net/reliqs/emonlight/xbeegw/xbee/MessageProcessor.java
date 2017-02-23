package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.digi.xbee.api.utils.ByteUtils;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

public abstract class MessageProcessor {
//	private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

	private final Processor processor;

	MessageProcessor(Processor processor) {
		super();
		this.processor = processor;
	}

	abstract void process(DataMessage m, NodeState ns, byte selector, ByteBuffer in);

	/**
	 * Trigger called when a threshold is trespassed. The parameters type and
	 * enable define the new state of the alarm level that needs to be set.
	 * 
	 * @param ns
	 * @param p
	 * @param t
	 * @param enable
	 */
	public void trigger(NodeState ns, Probe p, Type t, boolean enable) {
		throw new UnsupportedOperationException("trigger not implemented");
	}

	protected void sendOK(NodeState ns) {
		byte[] b = ByteUtils.stringToByteArray("OK");
		processor.sendData(ns, b);
	}

	protected void sendDeviceConfiguration(NodeState ns, DeviceConfig storedCfg) {
		processor.sendData(ns, storedCfg.buildResponse());
	}

	protected Instant calculateTimeFromDataMessage(NodeState ns, int t) {
		long dt = Integer.toUnsignedLong(ns.dataTimeMSec - t);
		Instant time = ns.dataTime.minus(dt, ChronoUnit.MILLIS);
		return time;
	}

	protected void verifyTime(Node node, Instant time) {
		Instant now = Instant.now().plus(1, ChronoUnit.SECONDS);
		assert now.isAfter(time) : "received message with timestamp ahead of now";
		assert now.minus(node.getSampleTime() * 2, ChronoUnit.MILLIS).isBefore(time) : String
				.format("received message timestamp %s is too old compared to now %s", time, now);
	}

	protected void sendBuzzerAlarmLevel(NodeState ns, int level) {
		ByteBuffer b = ByteBuffer.allocate(3);
		b.put((byte) 'S');
		b.put((byte) 'B');
		b.put((byte) level);
		processor.sendData(ns, b.array());
	}

}
