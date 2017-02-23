package net.reliqs.emonlight.xbeegw.xbee.state;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeData;
import net.reliqs.emonlight.xbeegw.xbee.data.ProbeDataFactory;

@Component
public class GlobalState {

	private final Map<String, NodeState> nodes;
	private final Map<Probe, ProbeData> probeDatas;

	@Autowired
	public GlobalState(Settings settings, ProbeDataFactory pdf) {
		super();
		this.nodes = new HashMap<>();
		this.probeDatas = new HashMap<>();
		// create node's state for each node
		settings.getNodes().forEach(n -> {
			NodeState ns = new NodeState(this, pdf, n);
			nodes.put(n.getAddress(), ns);
			ns.getProbeDatas().forEach(pd -> probeDatas.put(pd.getProbe(), pd));
		});
	}

	public NodeState getNodeState(String address) {
		return nodes.get(address);
	}

	public ProbeData getProbeData(Probe p) {
		return probeDatas.get(p);
	}

}
