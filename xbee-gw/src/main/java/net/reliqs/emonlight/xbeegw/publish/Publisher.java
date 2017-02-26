package net.reliqs.emonlight.xbeegw.publish;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergio on 25/02/17.
 */
@Component
public class Publisher {

    private List<Subscriber> subscribers;

    public Publisher() {
        this.subscribers = new ArrayList<>();
    }

    public void addSubscriber(Subscriber s) {
        subscribers.add(s);
    }

    public void publish(Probe probe, Data data) {
        for (Subscriber s: subscribers) {
            s.receive(probe, data);
        }
    }
}
