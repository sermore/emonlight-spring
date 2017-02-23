package net.reliqs.emonlight.xbeegw.xbee.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.PriorityQueue;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import net.reliqs.emonlight.xbeegw.xbee.MessageProcessor;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import net.reliqs.emonlight.xbeegw.xbee.state.GlobalState;

class ProbeDataImpl implements ProbeData {

	private final Probe probe;
	private final NodeState nodeState;
	private Deque<ProbeDataIn> pipeline;
	private MessageProcessor processor;
	private ProbeDataOut out;
	private Type previousTriggerState = null;
	private final PriorityQueue<Type> triggerState;

	private final GlobalState globalState;

	public ProbeDataImpl(GlobalState globalState, NodeState nodeState, Probe probe) {
		super();
		this.globalState = globalState;
		this.nodeState = nodeState;
		this.probe = probe;
		this.pipeline = new ArrayDeque<>();
		this.triggerState = new PriorityQueue<>();
		if (probe.getType().ordinal() > Type.VCC.ordinal()) {
			addToPipeline(probe.getType());
		}
		if (probe.getSoftThreshold() > 0) {
			addToPipeline(Type.M_SOFT_THRESHOLD_1);
			addToPipeline(Type.M_SOFT_THRESHOLD_2);
			addToPipeline(Type.M_SOFT_THRESHOLD_3);
		}
		if (probe.getHardThreshold() > 0) {
			addToPipeline(Type.M_HARD_THRESHOLD);
		}
		if (probe.isConnectedToOutput()) {
			ProbeDataWithQueueing q = new ProbeDataWithQueueing();
			pipeline.add(q);
			out = q;
		} else
			out = null;
	}

	private void addToPipeline(Type type) {
		switch (type) {
		case PULSE:
		case SAMPLE:
		case DHT22_H:
		case DHT22_T:
		case VCC:
			break;
		case F_MEAN_5MIN:
			pipeline.add(new ProbeDataAveraged(this, 300));
			break;
		case F_MEAN_15MIN:
			pipeline.add(new ProbeDataAveraged(this, 900));
			break;
		case M_SOFT_THRESHOLD_1:
			pipeline.add(new ProbeDataAveraged(this, 1));
			pipeline.add(new ProbeDataMonitorThreshold(this, Type.M_SOFT_THRESHOLD_1, probe.getSoftThreshold()));
			break;
		case M_SOFT_THRESHOLD_2:
			pipeline.add(new ProbeDataAveraged(this, probe.getSoftThresholdTimeSec() * 920 / 10800)); // 15
																										// *
																										// 60
																										// +
																										// 20
			pipeline.add(new ProbeDataMonitorThreshold(this, Type.M_SOFT_THRESHOLD_2, probe.getSoftThreshold()));
			break;
		case M_SOFT_THRESHOLD_3:
			pipeline.add(new ProbeDataAveraged(this, probe.getSoftThresholdTimeSec() * 1840 / 10800)); // 30
																										// *
																										// 60
																										// +
																										// 40
			pipeline.add(new ProbeDataMonitorThreshold(this, Type.M_SOFT_THRESHOLD_3, probe.getSoftThreshold()));
			break;
		case M_HARD_THRESHOLD:
			pipeline.add(new ProbeDataAveraged(this, 1));
			pipeline.add(new ProbeDataMonitorThreshold(this, Type.M_HARD_THRESHOLD, probe.getHardThreshold()));
			break;
		default:
			throw new UnsupportedOperationException("filter not implemented");
		}
	}

	void setProcessor(MessageProcessor processor) {
		this.processor = processor;
	}

	public MessageProcessor getProcessor() {
		return processor;
	}

	@Override
	public Probe getProbe() {
		return probe;
	}

	@Override
	public NodeState getNodeState() {
		return nodeState;
	}

	private Data add(Data in) {
		// FIXME not synchronized
		if (in == null)
			return null;

		Data firstIn = in;
		Data out = in;
		for (ProbeDataIn pi : pipeline) {
			out = pi.add(pi.useFirstIn() ? firstIn : in);
			in = out;
		}
		if (probe.getFilters() != null)
			for (Probe f : probe.getFilters()) {
				globalState.getProbeData(f).add(processor, firstIn);
			}
		if (probe.hasThresholds())
			trigger();
		return firstIn;
	}

	private void trigger() {
		assert processor != null;
		boolean lowerTriggerIsActive = triggerState.contains(Type.M_SOFT_THRESHOLD_1);
		Type newTriggerState = triggerState.peek();
		if (!lowerTriggerIsActive) {
			// if lower trigger is not active and we came from an active state,
			// then we trigger goes off, no matter what
			// trigger was active before
			if (previousTriggerState != null) {
				processor.trigger(nodeState, probe, previousTriggerState, false);
				previousTriggerState = null;
			}
		} else if (newTriggerState != previousTriggerState) {
			// if the higher trigger is changed
			if (newTriggerState != null) {
				processor.trigger(nodeState, probe, newTriggerState, true);
			} else {
				processor.trigger(nodeState, probe, previousTriggerState, false);
				assert false;
			}
			previousTriggerState = newTriggerState;
		}
	}

	@Override
	public void clear() {
		assert out != null;
		out.clear();
	}

	@Override
	public int queueLength() {
		assert out != null;
		return out.queueLength();
	}

	void trigger(Probe p, Type t, Data d, boolean enable) {
		if (enable) {
			triggerState.add(t);
		} else {
			triggerState.remove(t);
		}
	}

	@Override
	public void add(MessageProcessor processor, Data data) {
		this.processor = processor;
		add(data);
	}

	@Override
	public Data peek() {
		return out.peek();
	}

	@Override
	public Data poll() {
		return out.poll();
	}

	@Override
	public Iterator<Data> getIterator() {
		return out.getIterator();
	}

	@Override
	public boolean isEmpty() {
		return out.isEmpty();
	}

}
