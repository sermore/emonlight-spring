package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayDeque;
import java.util.Queue;

public class JpaService implements DeliveryService, ListenableFutureCallback<Integer> {
    private static final Logger log = LoggerFactory.getLogger(JpaService.class);

    private Queue<StoreData> queue, inFlight;
    private JpaAsyncService service;
    private boolean running;
    private int inFlightLength;


    public JpaService(JpaAsyncService service) {
        this.service = service;
        this.queue = new ArrayDeque<>();
        this.inFlight = new ArrayDeque<>();
        this.running = false;
        this.inFlightLength = 0;
    }

    @Override
    public void post() {
        if (!running) {
            running = true;
            if (inFlight.isEmpty()) {
                inFlight.addAll(queue);
                queue.clear();
            }
            inFlightLength = inFlight.size();
            log.trace("JPA: moved to inFlight {}", inFlightLength);
            ListenableFuture<Integer> res = service.post(inFlight);
            res.addCallback(this);
        } else {
            log.trace("JPA: saving ongoing, skipping post");
        }

    }

    @Override
    public boolean isReady() {
        log.trace("JPA: running={} q={}, inFlight={}", running, queue.size(), inFlight.size());
        return !running && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    public boolean isQueueEmpty() {
        log.trace("JPA: running={} q={}, inFlight={}", running, queue.size(), inFlight.size());
        return !running && queue.isEmpty() && inFlight.isEmpty();
    }

    @Override
    public void receive(Probe p, Probe.Type t, Data d) {
        queue.add(new StoreData(p, t, d));
    }

    @Override
    public void onSuccess(Integer result) {
        log.debug("JPA: q={}, saved={}/{}", queue.size(), result, inFlightLength);
        running = false;
        inFlightLength = 0;
        assert inFlight.isEmpty();
    }

    @Override
    public void onFailure(Throwable ex) {
        running = false;
        log.warn("JPA: FAIL q={}, inFlight={}/{}: {}", queue.size(), inFlight.size(), inFlightLength, ex.getMessage());
    }
}
