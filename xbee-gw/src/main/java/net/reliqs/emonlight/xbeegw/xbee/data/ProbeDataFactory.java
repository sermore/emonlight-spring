package net.reliqs.emonlight.xbeegw.xbee.data;

import org.springframework.stereotype.Service;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import net.reliqs.emonlight.xbeegw.xbee.state.GlobalState;

@Service
public class ProbeDataFactory {
	
	public ProbeData createProbeData(GlobalState gs, NodeState ns, Probe p) {
		return new ProbeDataImpl(gs, ns, p);
	}

}
