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
    private boolean enableBackup;
    private String backupPath;
    private int maxBatch;
    private boolean realTime;
    private long timeOutOnClose;
    private ListenableFuture<Integer> responseFromAsync;

    public AbstractService(AsyncService service, String logId, boolean enableBackup, String backupPath, int maxBatch,
            boolean realTime, long timeOutOnClose) {
        this.logId = logId;
        this.enableBackup = enableBackup;
        this.backupPath = backupPath;
        this.maxBatch = maxBatch;
        this.realTime = realTime;
        this.timeOutOnClose = timeOutOnClose;
        log = LoggerFactory.getLogger(this.getClass());
        this.service = service;
        this.queue = new LinkedList<>();
        this.inFlight = new LinkedList<>();
        this.inFlightLength = 0;
    }

    @Override
    public void post() {
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
        log.trace("{}: isReady running={} q={}, inFlight={}", logId, isRunning(), queue.size(), inFlight.size());
        return !isRunning() && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    @Override
    public boolean isQueueEmpty() {
        log.trace("{}: isQueueEmpty running={} q={}, inFlight={}", logId, isRunning(), queue.size(), inFlight.size());
        return !isRunning() && queue.isEmpty() && inFlight.isEmpty();
    }

    protected abstract E createData(Probe p, Probe.Type t, Data d);

    @Override
    public void receive(Probe p, Probe.Type t, Data d) {
        queue.add(createData(p, t, d));
        if (realTime) {
            post();
        }
    }

    @Override
    public void onSuccess(Integer result) {
        log.debug("{}: batch completed q={}, processed={}/{}", logId, queue.size(), result, inFlightLength);
        //        inFlightLength = 0;
        assert inFlight.size() + result == inFlightLength;
        inFlightLength = inFlight.size();
    }

    @Override
    public void onFailure(Throwable ex) {
        log.warn("{}: batch failed q={}, inFlight={}/{}: {}", logId, queue.size(), inFlight.size(), inFlightLength,
                ex.getMessage());
    }

    protected void onInit() {
        if (enableBackup) {
            String path = getBackupPath();
            if (Files.exists(Paths.get(path))) {
                ObjStoreToFile<LinkedList<E>> store = new ObjStoreToFile<>(path, true);
                List<LinkedList<E>> res = store.read();
                queue = res.get(0);
                inFlight = res.get(1);
                log.debug("{}: state {}/{} restored from {}", logId, queue.size(), inFlight.size(), path);
                return;
            }
        }
        log.debug("{}: init", logId);
        assert inFlight.isEmpty() && queue.isEmpty();
    }

    protected void onClose() {
        log.debug("{}: starting close phase", logId);
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
                    log.debug("{}: save state {}/{} to {}", logId, queue.size(), inFlight.size(), path);
                    if (!store.write()) {
                        log.error("{}: error saving state {}/{} saved to {}", logId, queue.size(), inFlight.size(),
                                path);
                    }
                }
            }
        }
        log.debug("{}: close phase completed", logId);
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

    void setEnableBackup(boolean enableBackup) {
        this.enableBackup = enableBackup;
    }

    public String getBackupPath() {
        return backupPath;
    }

    void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public int getMaxBatch() {
        return maxBatch;
    }

    void setMaxBatch(int maxBatch) {
        this.maxBatch = maxBatch;
    }

    public boolean isRealTime() {
        return realTime;
    }

    void setRealTime(boolean realTime) {
        this.realTime = realTime;
    }

    public long getTimeOutOnClose() {
        return timeOutOnClose;
    }

    void setTimeOutOnClose(long timeOutOnClose) {
        this.timeOutOnClose = timeOutOnClose;
    }
}