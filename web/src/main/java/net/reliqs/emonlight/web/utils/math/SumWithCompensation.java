package net.reliqs.emonlight.web.utils.math;

class SumWithCompensation {

    private double sum;
    private double sumCompensation; // Low order bits of sum

    public void sumWithCompensation(double value) {
        double tmp = value - sumCompensation;
        double velvel = sum + tmp; // Little wolf of rounding error
        sumCompensation = (velvel - sum) - tmp;
        sum = velvel;
    }

    public void sumWithCompensation(SumWithCompensation other) {
        sumWithCompensation(other.sum);
        sumWithCompensation(other.sumCompensation);
    }

    double sum() {
        return sum + sumCompensation;
    }

}
