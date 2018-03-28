package net.reliqs.emonlight.web.data;

import java.time.Instant;

public class Data {
    public final Instant t;
    public final Double v;

    public Data(Instant t, Double v) {
        this.t = t;
        this.v = v;
    }
}
