package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.data.Data;
import net.reliqs.emonlight.commons.utils.ObjStoreToFile;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class AbstractService<E extends Serializable, AsyncService extends AbstractAsyncService<E>>
        implements DeliveryService, ListenableFutureCallback<Integer> {

    protected final Logger log;
    private LinkedList<E> queue, inFlight;
    private AsyncService service;
    private int inFlightLength;
    private String logId;
    private boolean active;
    private boolean enableBackup;
    private String backupPath;
    private int maxBatch;
    private boolean realTime;
    private long timeOutOnClose;
    private int maxQueued;
    private ListenableFuture<Integer> responseFromAsync;
    private boolean lastStatus;

    public AbstractService(AsyncService service, String logId, boolean enableBackup, String backupPath, int maxBatch,
            boolean realTime, long timeOutOnClose, boolean active, int maxQueued) {
        log = LoggerFactory.getLogger(this.getClass());
        this.queue = new LinkedList<>();
        this.inFlight = new LinkedList<>();
        this.inFlightLength = 0;
        this.logId = logId;
        this.enableBackup = enableBackup;
        this.backupPath = backupPath;
        this.maxBatch = maxBatch;
        this.realTime = realTime;
        this.timeOutOnClose = timeOutOnClose;
        this.service = service;
        this.active = active;
        this.maxQueued = maxQueued;
        this.lastStatus = true;
    }

    @Override
    public void post() {
        if (!active) {
            return;
        }
        if (!isRunning()) {
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
            log.trace("{}: queue into inFlight {}", logId, inFlightLength);
            responseFromAsync = service.post(inFlight);
            responseFromAsync.addCallback(this);
        } else {
            log.trace("{}: post ongoing, skipping", logId);
        }
    }

    @Override
    public boolean isReady() {
        log.trace("{}: isReady running={}, active={}, q={}, inFlight={}", logId, isRunning(), active, queue.size(),
                inFlight.size());
        return active && !isRunning() && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    @Override
    public boolean isQueueEmpty() {
        log.trace("{}: isQueueEmpty running={}, active={} q={}, inFlight={}", logId, isRunning(), active, queue.size(),
                inFlight.size());
        return !active || !isRunning() && queue.isEmpty() && inFlight.isEmpty();
    }

    protected abstract E createData(Probe p, Probe.Type t, Data d);

    @Override
    public void receive(Probe p, Probe.Type t, Data d) {
        if (active) {
            if (maxQueued > 0 && queue.size() >= maxQueued) {
                queue.removeFirst();
            }
            queue.add(createData(p, t, d));
            if (realTime && lastStatus) {
                post();
            }
        }
    }

    @Override
    public void onSuccess(Integer result) {
        log.debug("{}: batch completed q={}, processed={}/{}", logId, queue.size(), result, inFlightLength);
        //        inFlightLength = 0;
        assert inFlight.size() + result == inFlightLength;
        inFlightLength = inFlight.size();
        lastStatus = true;
    }

    @Override
    public void onFailure(Throwable ex) {
        log.warn("{}: batch failed q={}, inFlight={}/{}: {}", logId, queue.size(), inFlight.size(), inFlightLength,
                ex.getMessage());
        lastStatus = false;
    }

    protected void onInit() {
        log.info("{}: initialization", logId);
        if (enableBackup) {
            String path = getBackupPath();
            if (Files.exists(Paths.get(path))) {
                ObjStoreToFile<LinkedList<E>> store = new ObjStoreToFile<>(path, true);
                List<LinkedList<E>> res = store.read();
                queue = res.get(0);
                inFlight = res.get(1);
                log.info("{}: state {}/{} restored from {}", logId, queue.size(), inFlight.size(), path);
                return;
            }
        }
        assert inFlight.isEmpty() && queue.isEmpty();
    }

    protected void onClose() {
        log.info("{}: starting close phase", logId);
        if (responseFromAsync != null && !responseFromAsync.isDone()) {
            log.warn("{}: async service is still running,  waiting {} ms for completion", logId, timeOutOnClose);
            try {
                responseFromAsync.get(timeOutOnClose, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("{}: failure on waiting for async service completion", logId, e);
            }
        }
        if (enableBackup) {
            if (!isQueueEmpty()) {
                if (responseFromAsync != null && !responseFromAsync.isDone()) {
                    log.error("{}: performing backup even if async service is still running!", logId);
                }
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
                    log.info("{}: save state {}/{} to {}", logId, queue.size(), inFlight.size(), path);
                    if (!store.write()) {
                        log.error("{}: error saving state {}/{} saved to {}", logId, queue.size(), inFlight.size(),
                                path);
                    }
                }
            }
        }
        log.info("{}: close phase completed", logId);
    }

    LinkedList<E> getQueue() {
        return queue;
    }

    LinkedList<E> getInFlight() {
        return inFlight;
    }

    public boolean isRunning() {
        return responseFromAsync != null && !responseFromAsync.isDone();
    }

    public boolean isEnableBackup() {
        return enableBackup;
    }

    protected void setEnableBackup(boolean enableBackup) {
        this.enableBackup = enableBackup;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public int getMaxBatch() {
        return maxBatch;
    }

    protected void setMaxBatch(int maxBatch) {
        this.maxBatch = maxBatch;
    }

    public boolean isRealTime() {
        return realTime;
    }

    public void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    public long getTimeOutOnClose() {
        return timeOutOnClose;
    }

    protected void setTimeOutOnClose(long timeOutOnClose) {
        this.timeOutOnClose = timeOutOnClose;
    }

    public AsyncService getService() {
        return service;
    }

    public String getLogId() {
        return logId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}