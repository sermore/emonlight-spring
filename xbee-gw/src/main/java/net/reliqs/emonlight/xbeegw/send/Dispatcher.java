package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.jms.JmsService;
import net.reliqs.emonlight.xbeegw.send.influxdb.InfluxdbService;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryServiceFactory;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private final Map<Server, DeliveryService> clients;
    private final GlobalState globalState;
    private Instant nextExecution;
    @Value("${dispatcher.rate:5000}")
    private long rate;

    @Autowired
    public Dispatcher(final Settings settings, Processor processor, GlobalState globalState, DeliveryServiceFactory rsFactory, JmsService jmsService, InfluxdbService influxdbService) {
        this.globalState = globalState;
        clients = new HashMap<>();
        // add REST services
        if (rsFactory != null) {
            settings.getServers().forEach(s -> {
                DeliveryService service = rsFactory.getSendService(s);
                processor.registerSubscriber(service);
                clients.put(s, service);
            });
        } else {
            log.info("exclude REST");
        }
        // add JMS
        if (jmsService != null) {
            processor.registerSubscriber(jmsService);
            clients.put(null, jmsService);
        } else {
            log.info("exclude JMS");
        }
        // add influxdb
        if (influxdbService != null) {
            processor.registerSubscriber(influxdbService);
            clients.put(null, influxdbService);
        } else {
            log.info("exclude influxDB");
        }

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
            clients.values().stream().filter(DeliveryService::isReady).forEach(DeliveryService::post);
        }
    }
}
