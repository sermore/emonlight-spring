package net.reliqs.emonlight.web.utils.math;

public class StandardDoubleSummaryStatistics implements DoubleSummaryStatistics {

    private java.util.DoubleSummaryStatistics doubleSummaryStatistics = new java.util.DoubleSummaryStatistics();

    @Override
    public void accept(double value, double weight) {
        doubleSummaryStatistics.accept(value);
    }

    @Override
    public void combine(DoubleSummaryStatistics otherBase) {
        StandardDoubleSummaryStatistics other = (StandardDoubleSummaryStatistics) otherBase;
        doubleSummaryStatistics.combine(other.doubleSummaryStatistics);
    }

    @Override
    public long getCount() {
        return doubleSummaryStatistics.getCount();
    }

    @Override
    public double getMin() {
        return doubleSummaryStatistics.getMin();
    }

    @Override
    public double getMax() {
        return doubleSummaryStatistics.getMax();
    }

    @Override
    public double getAverage() {
        return doubleSummaryStatistics.getAverage();
    }

    @Override
    public String toString() {
        return doubleSummaryStatistics.toString();
    }


}
