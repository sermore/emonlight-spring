package net.reliqs.emonlight.xbeegw.xbee.data;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

class ProbeDataAveraged extends ProbeDataWithFiltering {

	private int sec;
	protected boolean firstSkip = true;

	public ProbeDataAveraged(ProbeDataImpl root, int sec) {
		super(root);
		this.sec = sec;
	}

	private double calcAverage(int periodSec, Data in) {
		return in.v + Math.exp(-((in.t - last.t) / 1000.0) / periodSec) * (last.v - in.v);
	}

	@Override
	public Data add(Data in) {
		Node node = getProbe().getNode();
		double v = firstSkip ? in.v : calcAverage(sec, in);
		NodeState n = getNodeState();
//		log.debug("Filter {} AVG({}): v={}, dt={}, skip={} -> {}", probe.getName(), sec, in.v, in.t - last.t,
//				node != null && n.isSkipLastTime() || firstSkip, v);
		long t = in.t;
		last = new Data(t, v);
		if (node == null || !n.isSkipLastTime()) {
			if (firstSkip)
				firstSkip = false;
			return last;
		}
		return null;
	}

	@Override
	public boolean useFirstIn() {
		return true;
	}

}
