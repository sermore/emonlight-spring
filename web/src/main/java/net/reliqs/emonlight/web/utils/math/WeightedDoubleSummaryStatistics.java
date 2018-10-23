/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package net.reliqs.emonlight.web.utils.math;

import java.util.stream.Collector;

/**
 * A state object for collecting statistics such as count, min, max, sum, and
 * average.
 *
 * <p>This class is designed to work with (though does not require)
 * {@linkplain java.util.stream streams}. For example, you can compute
 * summary statistics on a stream of doubles with:
 * <pre> {@code
 * DoubleSummaryStatistics stats = doubleStream.collect(DoubleSummaryStatistics::new,
 *                                                      DoubleSummaryStatistics::accept,
 *                                                      DoubleSummaryStatistics::combine);
 * }</pre>
 *
 * <p>{@code DoubleSummaryStatistics} can be used as a
 * {@linkplain java.util.stream.Stream#collect(Collector) reduction}
 * target for a {@linkplain java.util.stream.Stream stream}. For example:
 *
 * <pre> {@code
 * DoubleSummaryStatistics stats = people.stream()
 *     .collect(Collectors.summarizingDouble(Person::getWeight));
 * }</pre>
 * <p>
 * This computes, in a single pass, the count of people, as well as the minimum,
 * maximum, sum, and average of their weights.
 *
 * @implNote This implementation is not thread safe. However, it is safe to use
 * {@link java.util.stream.Collectors#summarizingDouble(java.util.function.ToDoubleFunction)
 * Collectors.toDoubleStatistics()} on a parallel stream, because the parallel
 * implementation of {@link java.util.stream.Stream#collect Stream.collect()}
 * provides the necessary partitioning, isolation, and merging of results for
 * safe and efficient parallel execution.
 * @since 1.8
 */
public class WeightedDoubleSummaryStatistics implements DoubleSummaryStatistics {

    private static final long serialVersionUID = 1L;

    private long count;
    private SumWithCompensation sum, weightSum;
    private double simpleSum; // Used to compute right sum for non-finite inputs
    private double weightSimpleSum; // Used to compute right sum for non-finite inputs

    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /**
     * Construct an empty instance with zero count, zero sum,
     * {@code Double.POSITIVE_INFINITY} min, {@code Double.NEGATIVE_INFINITY}
     * max and zero average.
     */
    public WeightedDoubleSummaryStatistics() {
        sum = new SumWithCompensation();
        weightSum = new SumWithCompensation();
    }

    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     */
    @Override
    public void accept(double value, double weight) {
        ++count;
        double weightTimesValue = value * weight;
        this.simpleSum += weightTimesValue;
        weightSimpleSum += weight;
        sum.sumWithCompensation(weightTimesValue);
        weightSum.sumWithCompensation(weight);
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    /**
     * Combines the state of another {@code DoubleSummaryStatistics} into this
     * one.
     *
     * @param otherBase another {@code DoubleSummaryStatistics}
     * @throws NullPointerException if {@code other} is null
     */
    @Override
    public void combine(DoubleSummaryStatistics otherBase) {
        WeightedDoubleSummaryStatistics other = (WeightedDoubleSummaryStatistics) otherBase;
        count += other.count;
        simpleSum += other.simpleSum;
        sum.sumWithCompensation(other.sum);
        weightSum.sumWithCompensation(other.weightSum);
        min = Math.min(min, other.min);
        max = Math.max(max, other.max);
    }

    /**
     * Return the count of values recorded.
     *
     * @return the count of values
     */
    @Override
    public final long getCount() {
        return count;
    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     * <p>
     * If any recorded value is a NaN or the sum is at any point a NaN
     * then the sum will be NaN.
     *
     * <p> The value of a floating-point sum is a function both of the
     * input values as well as the order of addition operations. The
     * order of addition operations of this method is intentionally
     * not defined to allow for implementation flexibility to improve
     * the speed and accuracy of the computed result.
     * <p>
     * In particular, this method may be implemented using compensated
     * summation or other technique to reduce the error bound in the
     * numerical sum compared to a simple summation of {@code double}
     * values.
     *
     * @return the sum of values, or zero if none
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     */
    public final double getSum() {
        // Better error bounds to add both terms as the final sum
        double tmp = sum.sum();
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
        // If the compensated sum is spuriously NaN from
        // accumulating one or more same-signed infinite values,
        // return the correctly-signed infinity stored in
        // simpleSum.
        {
            return simpleSum;
        } else {
            return tmp;
        }
    }

    public final double getWeightSum() {
        // Better error bounds to add both terms as the final sum
        double tmp = weightSum.sum();
        if (Double.isNaN(tmp) && Double.isInfinite(weightSimpleSum))
        // If the compensated sum is spuriously NaN from
        // accumulating one or more same-signed infinite values,
        // return the correctly-signed infinity stored in
        // simpleSum.
        {
            return weightSimpleSum;
        } else {
            return tmp;
        }
    }

    /**
     * Returns the minimum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.POSITIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the minimum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.POSITIVE_INFINITY} if no values were
     * recorded
     */
    @Override
    public final double getMin() {
        return min;
    }

    /**
     * Returns the maximum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.NEGATIVE_INFINITY} if no values were
     * recorded. Unlike the numerical comparison operators, this method
     * considers negative zero to be strictly smaller than positive zero.
     *
     * @return the maximum recorded value, {@code Double.NaN} if any recorded
     * value was NaN or {@code Double.NEGATIVE_INFINITY} if no values were
     * recorded
     */
    @Override
    public final double getMax() {
        return max;
    }

    /**
     * Returns the arithmetic mean of values recorded, or zero if no
     * values have been recorded.
     * <p>
     * If any recorded value is a NaN or the sum is at any point a NaN
     * then the average will be code NaN.
     *
     * <p>The average returned can vary depending upon the order in
     * which values are recorded.
     * <p>
     * This method may be implemented using compensated summation or
     * other technique to reduce the error bound in the {@link #getSum
     * numerical sum} used to compute the average.
     *
     * @return the arithmetic mean of values, or zero if none
     * @apiNote Values sorted by increasing absolute magnitude tend to yield
     * more accurate results.
     */
    @Override
    public final double getAverage() {
        return getCount() > 0 ? getSum() / getWeightSum() : 0.0d;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a non-empty string representation of this object suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     */
    @Override
    public String toString() {
        return String.format("%s{count=%d, sum=%f, weightSum=%f, min=%f, average=%f, max=%f}", this.getClass().getSimpleName(), getCount(), getSum(),
                getWeightSum(), getMin(), getAverage(), getMax());
    }
}
