package net.reliqs.emonlight.xbeegw.send;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Queue;

public abstract class AbstractAsyncService<E> {

    protected final String logId;
    protected final Logger log;
    private int maxRetries;

    public AbstractAsyncService(String logId, int maxRetries) {
        this.logId = logId;
        this.log = LoggerFactory.getLogger(this.getClass());
        this.maxRetries = maxRetries;
    }

    protected abstract boolean send(E t);

    @Async
    @Transactional
    public ListenableFuture<Integer> post(Queue<E> inFlight) {
        int cnt = 0;
        int retries = 0;
        while (!inFlight.isEmpty() && retries < maxRetries) {
            E t = inFlight.peek();
            if (send(t)) {
                cnt++;
                inFlight.poll();
                retries = 0;
            } else {
                retries++;
            }
        }
        AsyncResult<Integer> res = new AsyncResult<>(cnt);
        return res;
    }

    public int getMaxRetries() {
        return maxRetries;
    }


}
