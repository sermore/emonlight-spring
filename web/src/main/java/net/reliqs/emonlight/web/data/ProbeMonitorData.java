package net.reliqs.emonlight.web.data;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.web.services.DataRepo;
import net.reliqs.emonlight.web.stats.StatsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ProbeMonitorData implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ProbeMonitorData.class);

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final Probe.Type type;
    private final int timeOutWarning;
    private final int timeOutDisconnected;
    //    private final Deque<Data> deque;
    private transient Data lastData = null;
    private Map<StatsData.StatType, StatsData> stats;
    //    private int status;
    private Instant lastT;
    private transient boolean initialized = false;

    public ProbeMonitorData(Probe p, String tzone) {
        this.id = p.getId();
        this.type = p.getType();
        this.timeOutWarning = p.getTimeout() / 2;
        this.timeOutDisconnected = p.getTimeout();
        //        this.deque = new ArrayDeque<>();
        this.lastT = Instant.EPOCH;
        this.initialized = false;
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
    }

    public long populate(DataRepo repo) {
        log.debug("populate probeMonitorData {}", getId());
        AtomicLong lastTLong = new AtomicLong(lastT.toEpochMilli());
        AtomicLong firstTLong = new AtomicLong(Instant.now().toEpochMilli());
        AtomicLong cnt = new AtomicLong(0L);
        repo.forEach(Arrays.asList(getId()), lastTLong.longValue(), Instant.now().toEpochMilli(), (id, v) -> {
            Instant t = Instant.ofEpochMilli((Long) v[0]);
            if (lastTLong.longValue() == 0) {
                lastTLong.set(t.toEpochMilli());
            }
            Instant lastT = Instant.ofEpochMilli(lastTLong.longValue());
            lastT = lastT.plus(1, ChronoUnit.HOURS);
            while (lastT.isBefore(t)) {
                check(lastT);
                lastT = lastT.plus(1, ChronoUnit.HOURS);
            }
            addData(new Data(t, (Double) v[1]));
            lastTLong.set(t.toEpochMilli());
            cnt.incrementAndGet();
        });
        lastT = Instant.ofEpochMilli(lastTLong.longValue());
        Instant t = this.lastT.plus(1, ChronoUnit.HOURS);
        Instant now = Instant.now();
        while (t.isBefore(now)) {
            this.lastT = t;
            check(t);
            t = t.plus(1, ChronoUnit.HOURS);
        }
        this.initialized = true;
        log.debug("probeMonitorData {} initialized, processed {} values", getId(), cnt.longValue());
        return cnt.longValue();
    }

    public int getStatus() {
        Instant now = Instant.now();
        Instant lastTime = lastData != null ? lastData.t : lastT;
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
        if (initialized) {
            // TODO check ranges
            addData(d);
        }
        return true;
        //        return deque.add(d);
    }

    private void addData(Data d) {
        lastData = d;
        for (StatsData s : stats.values()) {
            s.add(d.t, d.v);
        }
    }

    public Integer getId() {
        return id;
    }

}
