package net.reliqs.emonlight.xbeegw.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

abstract class DelayedEvent implements Delayed, Serializable {

    private static final long serialVersionUID = 1L;

    private long expireTime;
    private long delay;

    protected DelayedEvent(long delay) {
        this.delay = delay;
        this.expireTime = System.currentTimeMillis() + delay;
    }

    public boolean isScheduled() {
        return false;
    }

    public abstract boolean process();

    long getDelay() {
        return delay;
    }

    void reset() {
        this.expireTime = System.currentTimeMillis() + getDelay();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(expireTime, ((DelayedEvent) o).expireTime);
    }

    @Override
    public String toString() {
        return "t=" + Instant.ofEpochMilli(expireTime) + ", d=" + getDelay() + ", sc=" + isScheduled();
    }
}
