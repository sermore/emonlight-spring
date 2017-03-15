package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

class DelayProbe implements Delayed {

    private final Probe probe;
    private Instant expireTime;
    private int level;

    DelayProbe(Probe probe) {
        this.probe = probe;
        reset();
    }

    Probe getProbe() {
        return probe;
    }

    int getLevel() {
        return level;
    }

    void setLevel(int level) {
        this.level = level;
    }

    void reset() {
        this.expireTime = Instant.now().plus(getMaxTimeBetweenMessages(), ChronoUnit.MILLIS);
    }

    @Override
    public int compareTo(Delayed d) {
        long otherDelay = d.getDelay(TimeUnit.MILLISECONDS);
        long myDelay = getDelay(TimeUnit.MILLISECONDS);
        return Long.compare(myDelay, otherDelay);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireTime.toEpochMilli() - Instant.now().toEpochMilli(), TimeUnit.MILLISECONDS);
    }

    long getMaxTimeBetweenMessages() {
        return probe.getRealSampleTime() * 5;
    }

}