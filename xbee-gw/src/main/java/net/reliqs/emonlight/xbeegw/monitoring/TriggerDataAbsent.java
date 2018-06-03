package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.events.EventProcessorFacade;
import net.reliqs.emonlight.xbeegw.events.TriggerExpiredEvent;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TriggerDataAbsent extends Trigger {
    private static final Logger log = LoggerFactory.getLogger(TriggerDataAbsent.class);

    private EventProcessorFacade eventProcessorFacade;
    private int maxLevel;
    private Map<Node, TriggerExpiredEvent> map;
    private int resetLevelFired;

    TriggerDataAbsent(final Settings settings, Publisher publisher, EventProcessorFacade eventProcessorFacade,
            @Value("${triggerDataAbsent.maxLevel:10}") int maxLevel) {
        this.eventProcessorFacade = eventProcessorFacade;
        this.maxLevel = maxLevel;
        map = new HashMap<>();
        settings.getNodes().forEach(node -> {
            TriggerExpiredEvent event = new TriggerExpiredEvent(this, node);
            eventProcessorFacade.queueEvent(event);
            map.put(node, event);
            log.info("{}: registered to Trigger Data Missing, timeout {}", node, node.getTimeout());
        });
        publisher.addSubscriber(this);
    }

    void reset(TriggerExpiredEvent event, int newLevel) {
        event.setLevel(newLevel);
        eventProcessorFacade.eventReset(event);
        if (newLevel > maxLevel) {
            log.warn("{}: Trigger Data Missing level reached max, it won't be fired again: {}", event.getNode(), event);
        }
    }

    @Override
    void process(Probe probe, Data data) {
        TriggerExpiredEvent event = map.get(probe.getNode());
        // If the trigger was raised, then switch it off as a message is arrived
        reset(event, 0);
        resetLevelFired = 0;
        if (event.getLevel() > 0) {
            triggerChanged(probe, Type.DATA_MISSING_ALARM, event.getLevel(), 0);
        }
    }

    public void triggerFired(TriggerExpiredEvent event) {
        int newLevel = event.getLevel() + 1;
        log.warn("{}: Trigger Data Missing expired, new level {}", event.getNode(), newLevel);
        reset(event, newLevel);
        triggerDataAbsentChanged(event.getNode(), Type.DATA_MISSING_ALARM, event.getLevel(), newLevel);
        if (map.values().stream().allMatch(e -> e.getLevel() > resetLevelFired)) {
            resetLevelFired++;
            log.warn("Data missing for all devices, new level {}", resetLevelFired);
            triggerDataAbsentChanged(null, Type.DATA_MISSING_ALARM, event.getLevel(), resetLevelFired);
        }
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
