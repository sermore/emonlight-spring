package net.reliqs.emonlight.web.data;

import java.time.temporal.Temporal;

public class Data {
    public final Temporal t;
    public final Double v;

    public Data(Temporal t, Double v) {
        this.t = t;
        this.v = v;
    }
}
