package net.reliqs.emonlight.web.utils.math;

public class StandardDoubleSummaryStatistics implements DoubleSummaryStatistics {

    private static final long serialVersionUID = 1L;

    private long count;
    private double sum;
    private double sumCompensation; // Low order bits of sum
    private double simpleSum; // Used to compute right sum for non-finite inputs
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /**
     * Construct an empty instance with zero count, zero sum,
     * {@code Double.POSITIVE_INFINITY} min, {@code Double.NEGATIVE_INFINITY}
     * max and zero average.
     */
    public StandardDoubleSummaryStatistics() {
    }

    /**
     * Records another value into the summary information.
     *
     * @param value the input value
     */
    @Override
    public void accept(double value, double weight) {
        ++count;
        simpleSum += value;
        sumWithCompensation(value);
        min = Math.min(min, value);
        max = Math.max(max, value);
    }

    /**
     * Combines the state of another {@code DoubleSummaryStatistics} into this
     * one.
     *
     * @param otherGen another {@code DoubleSummaryStatistics}
     * @throws NullPointerException if {@code other} is null
     */
    public void combine(DoubleSummaryStatistics otherGen) {
        if (otherGen instanceof StandardDoubleSummaryStatistics) {
            StandardDoubleSummaryStatistics other = (StandardDoubleSummaryStatistics) otherGen;
            count += other.count;
            simpleSum += other.simpleSum;
            sumWithCompensation(other.sum);
            sumWithCompensation(other.sumCompensation);
            min = Math.min(min, other.min);
            max = Math.max(max, other.max);
        }
    }

    /**
     * Incorporate a new double value using Kahan summation /
     * compensated summation.
     */
    private void sumWithCompensation(double value) {
        double tmp = value - sumCompensation;
        double velvel = sum + tmp; // Little wolf of rounding error
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
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
        double tmp = sum + sumCompensation;
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum))
            // If the compensated sum is spuriously NaN from
            // accumulating one or more same-signed infinite values,
            // return the correctly-signed infinity stored in
            // simpleSum.
            return simpleSum;
        else
            return tmp;
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
    public final double getAverage() {
        return getCount() > 0 ? getSum() / getCount() : 0.0d;
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
        return String.format(
                "%s{count=%d, sum=%f, min=%f, average=%f, max=%f}",
                this.getClass().getSimpleName(),
                getCount(),
                getSum(),
                getMin(),
                getAverage(),
                getMax());
    }

}
