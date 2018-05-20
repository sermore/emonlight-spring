package net.reliqs.emonlight.web.stats;

import net.reliqs.emonlight.web.utils.math.DoubleSummaryStatistics;
import net.reliqs.emonlight.web.utils.math.StandardDoubleSummaryStatistics;
import net.reliqs.emonlight.web.utils.math.WeightedDoubleSummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StatsData {
    private static final Logger log = LoggerFactory.getLogger(StatsData.class);
    private static int[] sizes = new int[]{288, 288, 24, 24, 7, 7, 31, 31};
    private static EnumSet<StatType> overall = EnumSet.of(StatType.O_M5, StatType.O_HOUR, StatType.O_DAY_OF_WEEK, StatType.O_DAY_OF_MONTH);
    private Map<Integer, DoubleSummaryStatistics> stats;
    private boolean timeWeighted;
    private ZoneId zoneId;
    private StatType statType;
    private Integer lastK;
    private Instant lastT;

    public StatsData(StatType statType, boolean timeWeighted, ZoneId zoneId) {
        this.statType = statType;
        this.stats = new HashMap<>(sizes[statType.ordinal()]);
        this.timeWeighted = timeWeighted;
        this.zoneId = zoneId;
        for (int i = 0; i < sizes[statType.ordinal()]; i++) {
            stats.put(i, createStat());
        }
    }

    private DoubleSummaryStatistics createStat() {
        return timeWeighted ? new WeightedDoubleSummaryStatistics() : new StandardDoubleSummaryStatistics();
    }

    private Integer key(Instant tz) {
        ZonedDateTime t = tz.atZone(zoneId);
        switch (statType) {
            case O_M5:
            case M5:
                return t.get(ChronoField.MINUTE_OF_DAY) / 5;
            case O_HOUR:
            case HOUR:
                return t.get(ChronoField.HOUR_OF_DAY);
            case DAY_OF_WEEK:
            case O_DAY_OF_WEEK:
                return t.get(ChronoField.DAY_OF_WEEK) - 1;
            case O_DAY_OF_MONTH:
            case DAY_OF_MONTH:
                return t.get(ChronoField.DAY_OF_MONTH) - 1;
            default:
                Assert.state(false, String.format("statType not found %s", statType));
        }
        return null;
    }

    private String format(Integer k) {
        switch (statType) {
            case O_M5:
            case M5:
                return String.format("%02d:%02d", k / 12, (k % 12) * 5);
            case O_HOUR:
            case HOUR:
                return String.format("%02d", k);
            case O_DAY_OF_WEEK:
            case DAY_OF_WEEK:
                return DayOfWeek.MONDAY.plus(k).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            case O_DAY_OF_MONTH:
            case DAY_OF_MONTH:
                return String.format("%02d", k + 1);
            default:
                Assert.state(false, String.format("statType not found %s", statType));
        }
        return null;
    }

    public boolean isOverall() {
        return overall.contains(statType);
    }

    public DoubleSummaryStatistics getStat(Instant t) {
        Integer k = key(t);
        DoubleSummaryStatistics s;
        if (!k.equals(lastK)) {
            if (!overall.contains(statType) || stats.get(k) == null) {
                s = createStat();
                stats.put(k, s);
            } else {
                s = stats.get(k);
            }
            lastK = k;
        } else {
            s = stats.get(k);
        }
        log.trace("{} {} = {} {}", statType, k, s.getCount(), s.getAverage());
        return s;
    }

    public void add(Instant t, double v) {
        DoubleSummaryStatistics s = getStat(t);
        s.accept(v, calculateWeigth(t));
        lastT = t;
        //        log.trace("{} {} = {} {}", statType, k, s.getCount(), s.getAverage());
    }

    private double calculateWeigth(Instant t) {
        return timeWeighted ? (lastT == null ? 60_000 : t.toEpochMilli() - lastT.toEpochMilli()) : 0;
    }

    public Object[][] getAverage() {
        Instant now = Instant.now();
        Integer offset = key(now) + 1;
        int size = sizes[statType.ordinal()];
        Object[][] data = new Object[size][2];

        for (int i = 0; i < size; i++) {
            int oi = (i + offset) % size;
            data[i][0] = format(oi);
            data[i][1] = stats.get(oi).getAverage();
        }
        return data;
    }

    public enum StatType {M5, O_M5, HOUR, O_HOUR, DAY_OF_WEEK, O_DAY_OF_WEEK, DAY_OF_MONTH, O_DAY_OF_MONTH}
}
