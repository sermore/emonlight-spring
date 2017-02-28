package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;
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

    private List<Subscriber> subscribers;

    public Publisher() {
        this.subscribers = new ArrayList<>();
    }

    public void addSubscriber(Subscriber s) {
        subscribers.add(s);
    }

    public void publish(Probe probe, Data data) {
        log.trace("P {} {}", probe.getName(), data);
        for (Subscriber s: subscribers) {
            s.receive(probe, data);
        }
    }
}
