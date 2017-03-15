package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TriggerManager implements TriggerHandler {

	private Publisher publisher;
    private TriggerDataAbsent triggerDataAbsent;

	TriggerManager(Publisher publisher, TriggerDataAbsent triggerDataAbsent) {
		this.publisher = publisher;
        this.triggerDataAbsent = triggerDataAbsent;
        triggerDataAbsent.addHandler(this);
	}

    public void createTriggerLevel(NodeState ns, Probe p, TriggerHandler handler) {
        TriggerLevel tl = new TriggerLevel(p, ns, TriggerLevel.powerTriggers(p));
        tl.addHandler(handler);
        tl.addHandler(this);
        publisher.addSubscriber(tl);
    }
    
    public void registerTriggerDataAbsent(TriggerHandler handler) {
        triggerDataAbsent.addHandler(handler);
    }
    
	@Override
	public void triggerChanged(Probe probe, Type type, int oldValue, int newValue) {
        publisher.publish(probe, type, new Data(Instant.now().toEpochMilli(), newValue));
	}

    
}
