package net.reliqs.emonlight.xbeegw.publish;

public class Data {

    public final long t;
    public final double v;

    public Data(long t, double v) {
        super();
        this.t = t;
        this.v = v;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (t ^ (t >>> 32));
        long temp;
        temp = Double.doubleToLongBits(v);
        result = prime * result + (int) (temp ^ (temp >>> 32));
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
        Data other = (Data) obj;
        if (t != other.t)
            return false;
        if (Double.doubleToLongBits(v) != Double.doubleToLongBits(other.v))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("D [t=%s, v=%s]", t, v);
    }

}
