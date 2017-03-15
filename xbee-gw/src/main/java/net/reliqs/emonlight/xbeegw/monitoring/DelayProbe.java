package net.reliqs.emonlight.xbeegw.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import net.reliqs.emonlight.xbeegw.config.Probe;

class DelayProbe implements Delayed {

    private final Probe probe;
    private Instant expireTime;
    private int level;

    public DelayProbe(Probe probe) {
        this.probe = probe;
        reset();
    }
    
    public Probe getProbe() {
        return probe;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void reset() {
        this.expireTime = Instant.now().plus(getMaxTimeBetweenMessages(), ChronoUnit.MILLIS);
    }

    @Override
    public int compareTo(Delayed d) {
        return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireTime.toEpochMilli() - Instant.now().toEpochMilli() , TimeUnit.MILLISECONDS);
    }

    long getMaxTimeBetweenMessages() {
        return probe.getRealSampleTime() * 5;
    }
    
}