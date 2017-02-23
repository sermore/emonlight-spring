package net.reliqs.emonlight.xbeegw.xbee.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.Data;

class ProbeDataMonitorThreshold implements ProbeDataIn {
	protected static final Logger log = LoggerFactory.getLogger(ProbeDataMonitorThreshold.class);

	private final ProbeDataImpl root;
	private final double threshold;
	private final Type type;
	private boolean state;

	ProbeDataMonitorThreshold(ProbeDataImpl root, Type type, double threshold) {
		super();
		this.root = root;
		this.type = type;
		this.threshold = threshold;
		state = false;
	}

	@Override
	public Data add(Data d) {
		if (d != null) {
			Probe p = root.getProbe();
			if (!state && d.v >= threshold * 0.98) {
				state = true;
				trigger(p, d, true);
			} else if (state && d.v < threshold * 0.98) {
				state = false;
				trigger(p, d, false);
			}
		}
		return d;
	}

	void trigger(Probe p, Data d, boolean enable) {
		log.warn("{}: {}({}) {} {} => {}", p.getName(), type, threshold, enable, d.v, threshold * 0.98);
		root.trigger(p, type, d, enable);
	}

	@Override
	public boolean useFirstIn() {
		return false;
	}

}
