package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

public abstract class AbstractService<E extends Serializable, AsyncService extends AbstractAsyncService<E>> implements DeliveryService, ListenableFutureCallback<Integer> {

    protected final Logger log;
    private Queue<E> queue, inFlight;
    private AsyncService service;
    private boolean running;
    private int inFlightLength;
    private String logId;

    public AbstractService(AsyncService service, String logId) {
        this.logId = logId;
        log = LoggerFactory.getLogger(this.getClass());
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
            log.trace("{}: moved to inFlight {}", logId, inFlightLength);
            ListenableFuture<Integer> res = service.post(inFlight);
            res.addCallback(this);
        } else {
            log.trace("{}: saving ongoing, skipping post", logId);
        }

    }

    @Override
    public boolean isReady() {
        log.trace("{}: isReady running={} q={}, inFlight={}", logId, running, queue.size(), inFlight.size());
        return !running && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    public boolean isQueueEmpty() {
        log.trace("{}: isQueueEmpty running={} q={}, inFlight={}", logId, running, queue.size(), inFlight.size());
        return !running && queue.isEmpty() && inFlight.isEmpty();
    }

    protected abstract E createData(Probe p, Probe.Type t, Data d);

    @Override
    public void receive(Probe p, Probe.Type t, Data d) {
        queue.add(createData(p, t, d));
    }

    @Override
    public void onSuccess(Integer result) {
        log.debug("{}: batch completed q={}, saved={}/{}", logId, queue.size(), result, inFlightLength);
        running = false;
        inFlightLength = 0;
        assert inFlight.isEmpty();
    }

    @Override
    public void onFailure(Throwable ex) {
        running = false;
        log.warn("{}: batch failed q={}, inFlight={}/{}: {}", logId, queue.size(), inFlight.size(), inFlightLength, ex.getMessage());
    }

    @PreDestroy
    void onClose() {
        log.debug("{}: close", logId);
    }

}
