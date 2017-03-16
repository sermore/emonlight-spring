package net.reliqs.emonlight.xbeegw.monitoring;


import net.reliqs.emonlight.xbeegw.publish.Data;

/**
 * TODO implements lowerThan threshold.
 * <p>
 * Created by sergio on 26/02/17.
 */
public class SimpleTrigger {

    private AverageCalc calc;
    private double threshold, avgThreshold;

    public SimpleTrigger(AverageCalc calc, double threshold) {
        this.calc = calc;
        this.threshold = threshold;
        this.avgThreshold = 0.98 * threshold;
    }

    public boolean process(Data d) {
        double v;
        if (calc != null) {
            v = calc.process(d);
            return (v >= avgThreshold);
        } else {
            return d.v >= threshold;
        }
    }
}
