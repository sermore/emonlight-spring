package net.reliqs.emonlight.xbeegw.send.kafka;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

public class KafkaDeliveryService implements DeliveryService, ListenableFutureCallback<Map<String, Integer>> {
    private static final Logger log = LoggerFactory.getLogger(KafkaDeliveryService.class);

    private Queue<TopicData> queue;
    private Queue<TopicData> inFlight;
    private boolean running;
    private String serverName;
    private int cnt;
    private KafkaAsyncService service;
    private KafkaUtils ku;
    @Value("${gatewayId:0}")
    private int gatewayId;

    public KafkaDeliveryService(Settings settings, KafkaAsyncService service, KafkaUtils ku) {
        super();
        this.service = service;
        this.ku = ku;
        this.queue = new ArrayDeque<>();
        this.inFlight = new ArrayDeque<>();
        init(settings);
    }

    private void init(final Settings settings) {
        List<String> topics = getKafkaTopics(settings);
        ku.initTopics(topics);
        ku = null;
    }

    private List<String> getKafkaTopics(final Settings settings) {
        List<String> topics = settings.getProbes().map(p -> TopicData.getTopic(gatewayId, p)).collect(Collectors.toList());
        return topics;
    }

    @Override
    public void receive(Probe p, Probe.Type t, Data d) {
        queue.add(new TopicData(TopicData.getTopic(gatewayId, p), d));
    }

    @Override
    public void post() {
        // !!No thread-safe
        if (!running) {
            running = true;
            if (inFlight.isEmpty()) {
                inFlight.addAll(queue);
                queue.clear();
            }
            ListenableFuture<Map<String, Integer>> res = service.post(inFlight);
            res.addCallback(this);
        }
    }

    @Override
    public boolean isQueueEmpty() {
        return queue.isEmpty() && inFlight.isEmpty();
    }

    @Override
    public boolean isReady() {
        return !running && (!inFlight.isEmpty() || !queue.isEmpty());
    }

    @Override
    public void onSuccess(Map<String, Integer> result) {
        log.debug("KAFKA q={}, {}", queue.size(), result.entrySet().stream()
                .map(e -> e.getKey() + "(" + e.getValue() + ")").collect(Collectors.joining(", ")));
        running = false;
        assert inFlight.isEmpty();
    }

    @Override
    public void onFailure(Throwable ex) {
        running = false;
        log.warn("KAFKA FAIL q={}, inFlight={}: {}", queue.size(), inFlight.size(), ex.getMessage());
    }

}
