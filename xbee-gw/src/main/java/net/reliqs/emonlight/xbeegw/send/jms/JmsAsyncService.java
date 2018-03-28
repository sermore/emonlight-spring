package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.xbeegw.send.AbstractAsyncService;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.springframework.jms.core.JmsTemplate;

/**
 * Created by sergio on 27/02/17.
 */
public class JmsAsyncService extends AbstractAsyncService<StoreData> {

    private JmsTemplate jmsTemplate;

    public JmsAsyncService(JmsTemplate jmsTemplate, int maxRetries, boolean ignoreErrors) {
        super("JMS", maxRetries, ignoreErrors);
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    protected boolean send(StoreData data) {
        jmsTemplate.convertAndSend(data);
        return true;
    }

//    @Async
//    @Transactional
//    public ListenableFuture<Map<String, Integer>> post(Queue<StoreData> inFlight) {
//        Map<String, Integer> counters = new HashMap<String, Integer>();
//        while (!inFlight.isEmpty()) {
//            StoreData t = inFlight.peek();
//            send(t);
//            Integer cnt = counters.getOrDefault(t.findProbeByTypeAndPort(), 0);
//            counters.put(t.findProbeByTypeAndPort(), ++cnt);
//            log.trace("JMS OK probe={} cnt={}", t.findProbeByTypeAndPort(), cnt);
//            inFlight.poll();
//        }
//        AsyncResult<Map<String, Integer>> res = new AsyncResult<>(counters);
//        return res;
//    }

}
