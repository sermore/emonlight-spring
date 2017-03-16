package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.DelayQueue;

@Component
public class TriggerDataAbsent extends Trigger {
    private static final Logger log = LoggerFactory.getLogger(TriggerDataAbsent.class);

    private DelayQueue<DelayProbe> expires;
    private Map<Probe, DelayProbe> map;

    public TriggerDataAbsent(final Settings settings, Publisher publisher) {
        expires = new DelayQueue<>();
        map = new HashMap<>();
        settings.getProbes().forEach(p -> {
            DelayProbe dp = new DelayProbe(p);
            expires.add(dp);
            map.put(p, dp);
            log.debug("{}: registered to Trigger data absent, timeout {}", p, p.getTimeout());
        });
        publisher.addSubscriber(this);
    }

    void reset(DelayProbe p, int newLevel) {
        expires.remove(p);
        p.setLevel(newLevel);
        p.reset();
        expires.add(p);
    }

    @Override
    void process(Probe probe, Data data) {
        DelayProbe p = map.get(probe);
        // If the trigger was raised, then switch it off as a message is arrived
        reset(p, 0);
        if (p.getLevel() > 0) {
            triggerChanged(probe, Type.DATA_MISSING_ALARM, p.getLevel(), 0);
        }
    }

    @Scheduled(fixedRate = 1000)
    void checkTriggers() {
        DelayProbe p;
//        expires.stream().forEach(dp -> log.debug("{} {}", dp.getProbe().getName(), dp.getDelay(TimeUnit.MILLISECONDS)));
        do {
            p = expires.poll();
            if (p != null) {
                int newLevel = p.getLevel() + 1;
                log.debug("{}: Trigger Data Missing expired, new level {}", p.getProbe(), newLevel);
                reset(p, newLevel);
                triggerChanged(p.getProbe(), Type.DATA_MISSING_ALARM, p.getLevel(), newLevel);
            }
        } while (p != null);
    }

}
