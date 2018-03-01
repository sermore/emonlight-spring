package net.reliqs.emonlight.xbeegw.send;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Queue;

public abstract class AbstractAsyncService<E> {

    protected abstract boolean send(E t);

    @Async
    @Transactional
    public ListenableFuture<Integer> post(Queue<E> inFlight) {
        int cnt = 0;
        while (!inFlight.isEmpty()) {
            E t = inFlight.peek();
            if (send(t)) {
                cnt++;
            }
            inFlight.poll();
        }
        AsyncResult<Integer> res = new AsyncResult<>(cnt);
        return res;
    }

}
