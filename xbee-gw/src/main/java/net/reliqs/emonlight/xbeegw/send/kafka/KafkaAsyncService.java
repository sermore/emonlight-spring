package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.xbeegw.GwException;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

class KafkaAsyncService {
    private static final Logger log = LoggerFactory.getLogger(KafkaAsyncService.class);

    private KafkaTemplate<Long, Double> kafkaTemplate;

    KafkaAsyncService(KafkaTemplate<Long, Double> kafkaTemplate) {
        super();
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    ListenableFuture<Map<String, Integer>> post(Queue<TopicData> queue) {
        Map<String, Integer> counters = new HashMap<String, Integer>();
        while (!queue.isEmpty()) {
            TopicData k = queue.peek();
            ListenableFuture<SendResult<Long, Double>> res = send(k.topic, k.data);
            // try {
            SendResult<Long, Double> sendResult;
            try {
                sendResult = res.get();
                RecordMetadata m = sendResult.getRecordMetadata();
                Integer cnt = counters.getOrDefault(k.topic, 0);
                counters.put(k.topic, ++cnt);
                log.trace("KAFKA OK topic={}, offset={}", m.topic(), m.offset());
                queue.poll();
            } catch (InterruptedException | ExecutionException e) {
                throw new GwException(e);
            }
            // } catch (InterruptedException | ExecutionException e) {
            // log.warn(String.format("error sending data %s:%s => %s", k.topic,
            // k.data, e.getMessage()), e);
            // ok = false;
            // }
        }
        AsyncResult<Map<String, Integer>> res = new AsyncResult<>(counters);
        return res;
    }

    ListenableFuture<SendResult<Long, Double>> send(String topic, Data data) {
        log.trace("KAFKA topic={}, data={}", topic, data);
        ListenableFuture<SendResult<Long, Double>> future = kafkaTemplate.send(topic, data.t, data.v);
        return future;
    }

}
