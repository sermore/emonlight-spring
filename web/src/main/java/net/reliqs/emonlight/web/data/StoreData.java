package net.reliqs.emonlight.web.data;

import java.io.Serializable;
import java.util.Objects;

public class StoreData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String node;
    private String probe;
    private String type;
    private long t;
    private double v;

    public StoreData() {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StoreData storeData = (StoreData) o;
        return t == storeData.t && Double.compare(storeData.v, v) == 0 && Objects.equals(node, storeData.node) &&
                Objects.equals(probe, storeData.probe) && Objects.equals(type, storeData.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(node, probe, type, t, v);
    }

    @Override
    public String toString() {
        return "StoreData{" + "node='" + node + '\'' + ", probe='" + probe + '\'' + ", type='" + type + '\'' + ", t=" +
                t + ", v=" + v + '}';
    }
}