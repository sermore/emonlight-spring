package net.reliqs.emonlight.xbeegw.xbee.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

abstract class ProbeDataWithFiltering implements ProbeDataIn {
	protected static final Logger log = LoggerFactory.getLogger(ProbeDataWithFiltering.class);

	protected final ProbeDataImpl root;
	protected Data last = new Data(0, 0);

	ProbeDataWithFiltering(ProbeDataImpl root) {
		this.root = root;
	}

	protected Probe getProbe() {
		return root.getProbe();
	}
	
	protected NodeState getNodeState() {
		return root.getNodeState();
	}

}
