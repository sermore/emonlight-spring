package net.reliqs.emonlight.web.utils.math;

import java.io.Serializable;

public interface DoubleSummaryStatistics extends Serializable {
    void accept(double value, double weight);

    void combine(DoubleSummaryStatistics other);

    long getCount();

    double getMin();

    double getMax();

    double getAverage();
}
