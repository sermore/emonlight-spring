package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private final GlobalState globalState;
    private Instant nextExecution;
    @Value("${dispatcher.rate:5000}")
    private long rate;
    private Publisher publisher;

    public Dispatcher(final Settings settings, Processor processor, GlobalState globalState, Publisher publisher) {
        log.debug("init dispatcher");
        this.publisher = publisher;
        this.globalState = globalState;
        nextExecution = Instant.now().plus(rate, ChronoUnit.MILLIS);
    }

    /**
     * Deliver queued data.
     * No Thread-Safe, must be called in main thread.
     */
    public void process() {
        Instant now = Instant.now();
        if (nextExecution.isBefore(now)) {
            nextExecution = now.plus(rate, ChronoUnit.MILLIS);
            publisher.getServices().stream().filter(DeliveryService::isReady).forEach(DeliveryService::post);
        }
    }

}
