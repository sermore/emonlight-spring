package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.commons.data.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


class NodeDataJSON {

    private String k;
    private int id;
    private List<Number[]> d;

    public NodeDataJSON() {
        super();
        d = new ArrayList<>();
    }

    public NodeDataJSON(int id, String k) {
        this();
        this.k = k;
        this.id = id;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Number[]> getD() {
        return d;
    }

    public void setD(List<Number[]> d) {
        this.d = d;
    }

    public Number[] addData(Data in) {
        Number[] v = new Number[]{(in.t / 1000L), ((in.t % 1000L) * 1_000_000L), in.v};
        d.add(v);
        return v;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((d == null) ? 0 : d.hashCode());
        result = prime * result + id;
        result = prime * result + ((k == null) ? 0 : k.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeDataJSON other = (NodeDataJSON) obj;
        if (d == null) {
            if (other.d != null)
                return false;
        } else {
            if (d.size() != other.d.size())
                return false;
            for (int i = 0; i < d.size(); i++) {
                if (!Arrays.equals(d.get(i), other.d.get(i)))
                    return false;
            }
        }
        if (id != other.id)
            return false;
        if (k == null) {
            if (other.k != null)
                return false;
        } else if (!k.equals(other.k))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NodeData [k=" + k + ", id=" + id + ", d="
                + d.stream().map(v -> Arrays.toString(v)).collect(Collectors.joining()) + "]";
    }

}
