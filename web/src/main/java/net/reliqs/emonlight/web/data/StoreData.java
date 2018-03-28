package net.reliqs.emonlight.web.data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

public class StoreData implements Serializable {

    private static final long serialVersionUID = 2L;

    @NotNull
    @Min(1)
    private Integer node;
    @NotNull
    @Min(1)
    private Integer probe;
    @NotNull
    private String type;
    @Min(1)
    private long t;
    private double v;

    public StoreData() {
    }

    public Integer getNode() {
        return node;
    }

    public void setNode(Integer node) {
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getProbe() {
        return probe;
    }

    public void setProbe(Integer probe) {
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