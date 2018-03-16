package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.state.ObjStoreToFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractService<E extends Serializable, AsyncService extends AbstractAsyncService<E>> implements DeliveryService, ListenableFutureCallback<Integer> {

    protected final Logger log;
    private LinkedList<E> queue, inFlight;
    private AsyncService service;
    private boolean running;
    private int inFlightLength;
    private String logId;
    private int maxBatch;

    public AbstractService(AsyncService service, String logId, int maxBatch) {
        this.logId = logId;
        this.maxBatch = maxBatch;
        log = LoggerFactory.getLogger(this.getClass());
        this.service = service;
        this.queue = new LinkedList<>();
        this.inFlight = new LinkedList<>();
        this.running = false;
        this.inFlightLength = 0;
    }

    @Override
    public void post() {
        if (!running) {
            running = true;
            if (inFlight.isEmpty()) {
                if (maxBatch > 0 && queue.size() > maxBatch) {
                    List<E> sublist = queue.subList(0, maxBatch);
                    inFlight.addAll(sublist);
                    sublist.clear();
                } else {
                    inFlight.addAll(queue);
                    queue.clear();
                }
            }
            inFlightLength = inFlight.size();
            log.trace("{}: moved to inFlight {}", logId, inFlightLength);
            ListenableFuture<Integer> res = service.post(inFlight);
            res.addCallback(this);
        } else {
            log.trace("{}: post ongoing, skipping", logId);
        }

    }

    @Override
    public boolean isReady() {
        log.trace("{}: isReady running={} q={}, inFlight={}", logId, running, queue.size(), inFlight.size());
        return !running && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    @Override
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

    private String getBackupPath() {
        return this.getClass().getSimpleName() + ".dat";
    }

    protected void onInit() {
        String path = getBackupPath();
        if (Files.exists(Paths.get(path))) {
            ObjStoreToFile<LinkedList<E>> store = new ObjStoreToFile<>(path, true);
            List<LinkedList<E>> res = store.read();
            queue = res.get(0);
            inFlight = res.get(1);
            log.debug("{}: state {}/{} restored from {}", logId, queue.size(), inFlight.size(), path);
        } else {
            log.debug("{}: init", logId);
            assert inFlight.isEmpty() && queue.isEmpty();
        }
    }

    protected void onClose() {
        if (!isQueueEmpty()) {
            String path = getBackupPath();
            ObjStoreToFile<LinkedList<E>> store = new ObjStoreToFile<>(path, false);
            if (queue != null && !queue.isEmpty()) {
                store.add(queue);
            } else {
                store.add(new LinkedList<>());
            }
            if (inFlight != null && !inFlight.isEmpty()) {
                store.add(inFlight);
            } else {
                store.add(new LinkedList<>());
            }
            if (!store.isEmpty()) {
                if (store.write()) {
                    log.debug("{}: state {}/{} saved to {}", logId, queue.size(), inFlight.size(), path);
                } else {
                    log.error("{}: error saving state {}/{} saved to {}", logId, queue.size(), inFlight.size(), path);
                }
            }
        }
        log.debug("{}: close", logId);
    }

}
