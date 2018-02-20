package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.xbeegw.xbee.DataMessage;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

class DelayedEvent implements Delayed {

    enum EventType {Message, Dispatcher, Stop}

    ;

    private long startTime;
    private EventType eventType;
    private DataMessage msg;

    protected DelayedEvent(long delay) {
        this.startTime = System.currentTimeMillis() + delay;
    }

    DelayedEvent(DataMessage msg, long delay) {
        this(delay);
        this.msg = msg;
        this.eventType = EventType.Message;
    }

    DelayedEvent(EventType eventType, long delay) {
        this(delay);
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public DataMessage getMsg() {
        return msg;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(startTime, ((DelayedEvent) o).startTime);
    }

    @Override
    public String toString() {
        return "E{" +
                "t=" + startTime +
                ", e=" + eventType +
                (msg != null ? ", msg=" + msg : "") +
                '}';
    }
}
