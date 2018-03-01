package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergio on 25/02/17.
 */
@Service
public class Publisher {
    private static final Logger log = LoggerFactory.getLogger(Publisher.class);

    private List<DeliveryService> services;
    private List<Subscriber> subscribers;

    public Publisher() {
        this.subscribers = new ArrayList<>();
        this.services = new ArrayList<>();
    }

    public void addService(DeliveryService s) {
        log.debug("register service: {}", s);
        services.add(s);
    }

    public void addSubscriber(Subscriber s) {
        log.debug("register subscriber: {}", s);
        subscribers.add(s);
    }

    public void publish(Probe probe, Type type, Data data) {
        log.trace("P {} {} {}", probe.getName(), type, data);
        for (Subscriber s : subscribers) {
            s.receive(probe, type, data);
        }
        for (Subscriber s : services) {
            s.receive(probe, type, data);
        }
    }

    public List<DeliveryService> getServices() {
        return services;
    }
}
