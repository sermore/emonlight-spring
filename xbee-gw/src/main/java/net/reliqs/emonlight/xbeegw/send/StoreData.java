package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;

import java.io.Serializable;

public class StoreData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String node;
    private String probe;
    private String type;
    private long t;
    private double v;

    public StoreData() {
    }

    public StoreData(final Probe probe, final Type type, final Data data) {
        super();
        this.probe = probe.getName();
        this.node = probe.getNode().getName();
        this.type = probe.getType().name();
        this.t = data.t;
        this.v = data.v;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "StoreData{" +
                "node='" + node + '\'' +
                ", probe='" + probe + '\'' +
                ", type='" + type + '\'' +
                ", t=" + t +
                ", v=" + v +
                '}';
    }
}