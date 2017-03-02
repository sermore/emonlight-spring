package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;

public class StoreData {

	private String probe;
	private long t;
	private double v;

    public StoreData() {
    }

    public StoreData(final Probe probe, final Data data) {
		super();
		this.probe = probe.getName();
		this.t = data.t;
		this.v = data.v;
	}

    public String getProbe() {
        return probe;
    }

    public void setProbe(String probe) {
        this.probe = probe;
    }

    public long getT() {
        return t;
    }

    public void setT(long t) {
        this.t = t;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }
}