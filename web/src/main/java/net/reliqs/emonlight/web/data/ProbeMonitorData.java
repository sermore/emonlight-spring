package net.reliqs.emonlight.web.data;

import net.reliqs.emonlight.commons.config.Probe;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ProbeMonitorData {

    private final Integer id;
    private final Probe.Type type;
    private final int timeOutWarning, timeOutDisconnected;
    //    private final Deque<Data> deque;
    private Data lastData;
    private int status;
    private Instant startInstant;

    public ProbeMonitorData(Probe p) {
        this.id = p.getId();
        this.type = p.getType();
        this.timeOutWarning = p.getTimeout() / 2;
        this.timeOutDisconnected = p.getTimeout();
        //        this.deque = new ArrayDeque<>();
        this.startInstant = Instant.now();
        //        this.lastData = new Data(Instant.EPOCH, 0.0);
    }

    public int getStatus() {
        Instant now = Instant.now();
        Instant lastTime = lastData != null ? lastData.t : startInstant;
        if (now.minus(timeOutWarning, ChronoUnit.MILLIS).isBefore(lastTime)) {
            return 0;
        } else if (now.minus(timeOutDisconnected, ChronoUnit.MILLIS).isAfter(lastTime)) {
            return 2;
        } else {
            return 1;
        }
    }

    public Data getLastData() {
        return lastData;
    }

    public Instant getLastT() {
        return lastData != null ? lastData.t : null;
    }

    public Double getLastV() {
        return lastData != null ? lastData.v : null;
    }

    //    public Deque<Data> getDeque() {
    //        return deque;
    //    }

    public Probe.Type getType() {
        return type;
    }

    public boolean add(Data d) {
        lastData = d;
        return true;
        //        return deque.add(d);
    }

}
