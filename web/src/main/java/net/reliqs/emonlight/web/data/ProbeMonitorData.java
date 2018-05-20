package net.reliqs.emonlight.web.data;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.web.stats.StatsData;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbeMonitorData {

    private final Integer id;
    private final Probe.Type type;
    private final int timeOutWarning;
    private final int timeOutDisconnected;
    //    private final Deque<Data> deque;
    private Data lastData;
    private Map<StatsData.StatType, StatsData> stats;
    private int status;
    private Instant startInstant;

    public ProbeMonitorData(Probe p, String tzone, List<Number[]> initData, Instant tstart) {
        this.id = p.getId();
        this.type = p.getType();
        this.timeOutWarning = p.getTimeout() / 2;
        this.timeOutDisconnected = p.getTimeout();
        //        this.deque = new ArrayDeque<>();
        this.startInstant = Instant.now();
        //        this.lastData = new Data(Instant.EPOCH, 0.0);
        this.stats = new HashMap<>();
        boolean timeWeighted = p.getType() == Probe.Type.PULSE;
        ZoneId zoneId = ZoneId.of(tzone == null ? "Z" : tzone);
        //        stats.put(StatType.M5, new StatsData(StatType.M5, timeWeighted, zoneId));
        stats.put(StatsData.StatType.HOUR, new StatsData(StatsData.StatType.HOUR, timeWeighted, zoneId));
        stats.put(StatsData.StatType.DAY_OF_WEEK, new StatsData(StatsData.StatType.DAY_OF_WEEK, timeWeighted, zoneId));
        stats.put(StatsData.StatType.DAY_OF_MONTH, new StatsData(StatsData.StatType.DAY_OF_MONTH, timeWeighted, zoneId));
        //        stats.put(StatType.O_M5, new StatsData(StatType.O_M5, timeWeighted, zoneId));
        stats.put(StatsData.StatType.O_HOUR, new StatsData(StatsData.StatType.O_HOUR, timeWeighted, zoneId));
        stats.put(StatsData.StatType.O_DAY_OF_WEEK, new StatsData(StatsData.StatType.O_DAY_OF_WEEK, timeWeighted, zoneId));
        stats.put(StatsData.StatType.O_DAY_OF_MONTH, new StatsData(StatsData.StatType.O_DAY_OF_MONTH, timeWeighted, zoneId));
        Instant now = Instant.now();
        if (initData != null) {
            Instant lastT = tstart;
            for (Number[] v : initData) {
                Instant t = Instant.ofEpochMilli((Long) v[0]);
                lastT = lastT.plus(1, ChronoUnit.HOURS);
                while (lastT.isBefore(t)) {
                    check(lastT);
                    lastT = lastT.plus(1, ChronoUnit.HOURS);
                }
                add(new Data(t, (Double) v[1]));
                lastT = t;
            }
            lastT = lastT.plus(1, ChronoUnit.HOURS);
            while (lastT.isBefore(now)) {
                check(lastT);
                lastT = lastT.plus(1, ChronoUnit.HOURS);
            }
        }
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

    public StatsData getStats(StatsData.StatType statType) {
        return stats.get(statType);
    }

    public void check(Instant t) {
        for (StatsData s : stats.values()) {
            s.getStat(t);
        }
    }

    public boolean add(Data d) {
        // TODO check ranges
        lastData = d;
        for (StatsData s : stats.values()) {
            s.add(d.t, d.v);
        }
        return true;
        //        return deque.add(d);
    }

}
