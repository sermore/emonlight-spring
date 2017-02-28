package net.reliqs.emonlight.xbeegw.send.activemq;

import net.reliqs.emonlight.xbeegw.send.TopicData;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
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
    MessageConverter converter = new MappingJackson2MessageConverter();

    @Autowired
    public JmsAsyncService(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    void send(String topic, Data data) {
        jmsTemplate.send(topic, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                return converter.toMessage(data, session);
            }
        });
    }

    @Async
    @Transactional
    public ListenableFuture<Map<String, Integer>> post(Queue<TopicData> inFlight) {
        Map<String, Integer> counters = new HashMap<String, Integer>();
        while (!inFlight.isEmpty()) {
            TopicData t = inFlight.peek();
            send(t.topic, t.data);
            Integer cnt = counters.getOrDefault(t.topic, 0);
            counters.put(t.topic, ++cnt);
            log.trace("JMS OK topic={} cnt={}", t.topic, cnt);
            inFlight.poll();
        }
        AsyncResult<Map<String, Integer>> res = new AsyncResult<>(counters);
        return res;
    }
}
