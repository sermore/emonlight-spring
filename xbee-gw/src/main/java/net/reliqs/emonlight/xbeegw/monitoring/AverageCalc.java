package net.reliqs.emonlight.xbeegw.monitoring;


import net.reliqs.emonlight.xbeegw.publish.Data;

/**
 * Created by sergio on 25/02/17.
 */
public class AverageCalc {

    private int sec;
    private Data last;

    public AverageCalc(int sec) {
        this.sec = sec;
    }

    private double calcAverage(int periodSec, Data in) {
        return in.v + Math.exp(-((in.t - last.t) / 1000.0) / periodSec) * (last.v - in.v);
    }

    public double process(Data d) {
        double v = last == null ? d.v : calcAverage(sec, d);
        last = new Data(d.t, v);
        return v;
    }

    public double getValue() {
        return last != null ? last.v : 0;
    }
}
