package net.reliqs.emonlight.xbeegw.xbee;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import com.digi.xbee.api.RemoteXBeeDevice;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeDataFactory;
import net.reliqs.emonlight.xbeegw.xbee.state.GlobalState;

public class NodeState {
	// private static final Logger log =
	// LoggerFactory.getLogger(XbeeNode.class);

	private final Node node;
	private final Map<Type, ProbeData> probeDataMap;
	private final Deque<Instant> cfgRequests;
	private RemoteXBeeDevice device;
	// FIXME lastTimeMSec and lastTime work only for a single PULSE probe per
	// node
	int lastTimeMSec;
	Instant lastTime = Instant.EPOCH;
	Instant dataTime;
	int dataTimeMSec;
	boolean skipLastTime;
	long delta;

	public NodeState(GlobalState gs, ProbeDataFactory pdf, Node node) {
		this.node = node;
		this.probeDataMap = new HashMap<>();
		node.getProbes()
				.forEach(p -> probeDataMap.put(p.getType(), pdf.createProbeData(gs, this, p)));
		this.cfgRequests = new ArrayDeque<>();
	}

	RemoteXBeeDevice getDevice() {
		return device;
	}

	void setDevice(RemoteXBeeDevice device) {
		this.device = device;
	}

	public Node getNode() {
		return node;
	}

	public Probe getProbe(Type type) {
		ProbeData pd = probeDataMap.get(type);
		return pd != null ? pd.getProbe() : null;
	}

	public ProbeData getProbeData(Type type) {
		return probeDataMap.get(type);
	}

	public int getLastTimeMSec() {
		return lastTimeMSec;
	}

	public Instant getLastTime() {
		return lastTime;
	}

	public Instant getDataTime() {
		return dataTime;
	}

	public int getDataTimeMSec() {
		return dataTimeMSec;
	}

	public boolean isSkipLastTime() {
		return skipLastTime;
	}

	public long getDelta() {
		return delta;
	}

	public Collection<ProbeData> getProbeDatas() {
		return probeDataMap.values();
	}

	public boolean acceptConfigurationMessage(Instant time) {
		cfgRequests.addFirst(time);
		int s = cfgRequests.size();
		if (s > 4)
			cfgRequests.removeLast();
		if (s > 1) {
			Duration d = Duration.between(cfgRequests.peekFirst(), cfgRequests.peekLast());
			return d.getSeconds() < 5;
		}
		return true;
	}

	@Override
	public String toString() {
		return "XN [" + node.getName() + ", " + node.getAddress() + "]";
	}

}
