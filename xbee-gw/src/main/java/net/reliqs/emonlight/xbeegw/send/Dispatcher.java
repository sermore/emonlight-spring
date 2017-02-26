package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryServiceFactory;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Dispatcher {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private final Map<Server, DeliveryService> clients;
    private final GlobalState globalState;

    @Autowired
    public Dispatcher(final Settings settings, Processor processor, GlobalState globalState, DeliveryServiceFactory rsFactory) {
        this.globalState = globalState;
        clients = new HashMap<>();
        settings.getServers().forEach(s -> {
            DeliveryService service = rsFactory.getSendService(s);
            processor.registerSubscriber(service);
            clients.put(s, service);
        });
    }

    public void process() {
        clients.values().stream().filter(DeliveryService::isReady).forEach(DeliveryService::post);
    }
}
