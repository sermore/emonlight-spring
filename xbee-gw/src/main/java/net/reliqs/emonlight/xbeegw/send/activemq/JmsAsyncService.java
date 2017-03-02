package net.reliqs.emonlight.xbeegw.send.activemq;

import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * Created by sergio on 27/02/17.
 */
@Service
public class JmsAsyncService {
    private static final Logger log = LoggerFactory.getLogger(JmsAsyncService.class);

    private JmsTemplate jmsTemplate;

    @Autowired
    public JmsAsyncService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    void send(StoreData data) {
        jmsTemplate.convertAndSend(data);
    }

    @Async
    @Transactional
    public ListenableFuture<Map<String, Integer>> post(Queue<StoreData> inFlight) {
        Map<String, Integer> counters = new HashMap<String, Integer>();
        while (!inFlight.isEmpty()) {
            StoreData t = inFlight.peek();
            send(t);
            Integer cnt = counters.getOrDefault(t.getProbe(), 0);
            counters.put(t.getProbe(), ++cnt);
            log.trace("JMS OK probe={} cnt={}", t.getProbe(), cnt);
            inFlight.poll();
        }
        AsyncResult<Map<String, Integer>> res = new AsyncResult<>(counters);
        return res;
    }
}
