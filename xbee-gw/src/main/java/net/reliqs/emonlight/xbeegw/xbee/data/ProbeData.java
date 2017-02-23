package net.reliqs.emonlight.xbeegw.xbee.data;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import net.reliqs.emonlight.xbeegw.xbee.MessageProcessor;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

public interface ProbeData extends ProbeDataOut {

	void add(MessageProcessor processor, Data data);

	Probe getProbe();
	
	NodeState getNodeState();

}
