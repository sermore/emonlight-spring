package net.reliqs.emonlight.web.utils.math;

public interface DoubleSummaryStatistics {
    void accept(double value, double weight);

    void combine(DoubleSummaryStatistics other);

    long getCount();

    double getMin();

    double getMax();

    double getAverage();
}
