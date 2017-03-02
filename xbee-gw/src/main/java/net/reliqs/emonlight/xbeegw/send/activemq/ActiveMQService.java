package net.reliqs.emonlight.xbeegw.send.activemq;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by sergio on 25/02/17.
 */
@Service
public class ActiveMQService implements DeliveryService, ListenableFutureCallback<Map<String, Integer>> {
    private static final Logger log = LoggerFactory.getLogger(ActiveMQService.class);

    private Queue<StoreData> queue, inFlight;
    private boolean running;
    private JmsAsyncService service;

    @Autowired
    public ActiveMQService(JmsAsyncService service) {
        this.service = service;
        this.queue = new ArrayDeque<>();
        this.inFlight = new ArrayDeque<>();
    }

    @Override
    public void post() {
        if (!running) {
            running = true;
            if (inFlight.isEmpty()) {
                inFlight.addAll(queue);
                queue.clear();
            }
            ListenableFuture<Map<String, Integer>> res = service.post(inFlight);
            res.addCallback(this);
        }
    }

    @Override
    public boolean isReady() {
        return !running && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty() && inFlight.isEmpty();
    }

    @Override
    public void receive(Probe p, Data d) {
        queue.add(new StoreData(p, d));
    }

    @Override
    public void onSuccess(Map<String, Integer> result) {
        log.debug("JMS q={}, {}", queue.size(), result.entrySet().stream()
                .map(e -> e.getKey() + "(" + e.getValue() + ")").collect(Collectors.joining(", ")));
        running = false;
        assert inFlight.isEmpty();
    }

    @Override
    public void onFailure(Throwable ex) {
        running = false;
        log.warn("JMS FAIL q={}, inFlight={}: {}", queue.size(), inFlight.size(), ex.getMessage());
    }
}
