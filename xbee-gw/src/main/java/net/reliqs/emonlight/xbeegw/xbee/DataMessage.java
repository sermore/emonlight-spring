package net.reliqs.emonlight.xbeegw.xbee;

import java.time.Instant;

import com.digi.xbee.api.models.XBeeMessage;
import net.reliqs.emonlight.xbeegw.state.GlobalState;

class DataMessage {
	final XBeeMessage msg;
	final Instant time;
	final GlobalState globalState;

	DataMessage(GlobalState globalState, XBeeMessage msg) {
		super();
        this.globalState = globalState;
        this.msg = msg;
		this.time = Instant.now();
	}

	NodeState getNodeState() {
		return msg != null ? globalState.getNodeState(msg.getDevice().get64BitAddress().toString()) : null;
	}


}
