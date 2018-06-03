package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.events.EventProcessorFacade;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TriggerManager implements TriggerHandler {
    private static final Logger log = LoggerFactory.getLogger(TriggerManager.class);

    private Publisher publisher;
    private TriggerDataAbsent triggerDataAbsent;
    private EventProcessorFacade eventProcessorFacade;

    TriggerManager(Settings settings, Publisher publisher, TriggerDataAbsent triggerDataAbsent, EventProcessorFacade eventProcessorFacade,
            GlobalState globalState) {
        this.publisher = publisher;
        this.triggerDataAbsent = triggerDataAbsent;
        this.eventProcessorFacade = eventProcessorFacade;
        triggerDataAbsent.addHandler(this);
        settings.getNodes().forEach(n -> registerNode(globalState, n));
    }

    private void registerNode(GlobalState globalState, Node n) {
        log.info("register node {}", n);
        NodeState ns = globalState.getNodeState(n.getAddress());
        n.getProbes().stream().filter(Probe::hasThresholds).forEach(p -> createTriggerLevel(ns, p));
    }

    public void createTriggerLevel(NodeState ns, Probe p) {
        TriggerLevel tl = new TriggerLevel(p, ns, TriggerLevel.powerTriggers(p));
        tl.addHandler(this);
        publisher.addSubscriber(tl);
    }

    @Override
    public void triggerChanged(Probe probe, Type type, int oldValue, int newValue) {
        publisher.publish(probe, type, new Data(Instant.now().toEpochMilli(), newValue));
        switch (type) {
            case THRESOLD_ALARM:
                eventProcessorFacade.queueThresholdAlarmEvent(probe, newValue);
                break;
            default:
                log.error("unexpected type: {}", type);
        }
    }

    @Override
    public void triggerDataAbsentChanged(Node node, Type type, int oldValue, int newValue) {
        if (node != null) {
            eventProcessorFacade.queueMissingAlarmEvent(node, newValue);
        } else {
            eventProcessorFacade.queueResetEvent();
        }
    }


}
