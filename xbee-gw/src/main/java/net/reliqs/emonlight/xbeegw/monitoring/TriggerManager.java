package net.reliqs.emonlight.xbeegw.monitoring;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.publish.Subscriber;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

@Component
public class TriggerManager implements TriggerHandler {

//    private Map<Probe, TriggerLevel> triggers;
	private Publisher publisher;

	TriggerManager(Publisher publisher) {
		this.publisher = publisher;
//		this.triggers = new HashMap<>();
	}

    public void createTriggerLevel(NodeState ns, Probe p, TriggerHandler handler) {
        TriggerLevel tl = new TriggerLevel(p, ns, TriggerLevel.powerTriggers(p));
        tl.addHandler(handler);
        tl.addHandler(this);
        publisher.addSubscriber(tl);
//        triggers.put(p, tl);
    }
    
    public void createTriggerDataAbsent(TriggerHandler handler) {
        TriggerDataAbsent t = new TriggerDataAbsent();
        t.addHandler(handler);
        t.addHandler(this);
        publisher.addSubscriber(t);
    }
    
//	@Override
//	public void receive(Probe probe, Type type, Data data) {
//		// avoid loop in publish / subscribe
//        if (type == probe.getType()) {
//            TriggerLevel t = triggers.get(probe);
//            if (t != null) {
////                log.trace("{}: process trigger {}", probe.getNode(), type);
//                t.process(data);
//            }
//        }		
//	}

	@Override
	public void triggerChanged(Probe probe, Type type, int oldValue, int newValue) {
        publisher.publish(probe, type, new Data(Instant.now().toEpochMilli(), newValue));
	}

    
}
