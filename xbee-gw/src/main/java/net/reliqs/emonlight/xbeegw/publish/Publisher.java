package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergio on 25/02/17.
 */
@Component
public class Publisher {
    private static final Logger log = LoggerFactory.getLogger(Publisher.class);

    private List<DeliveryService> subscribers;

    public Publisher() {
        this.subscribers = new ArrayList<>();
    }

    public void addSubscriber(DeliveryService s) {
        log.debug("register: {}", s);
        subscribers.add(s);
    }

    public void publish(Probe probe, Type type, Data data) {
        log.trace("P {} {} {}", probe.getName(), type, data);
        for (Subscriber s: subscribers) {
            s.receive(probe, type, data);
        }
    }

    public List<DeliveryService> getServices() {
        return subscribers;
    }
}
