package net.reliqs.emonlight.xbeegw.send.kafka;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.xbee.Data;

@Service
public class KafkaDeliveryService implements DeliveryService, ListenableFutureCallback<Map<String, Integer>> {
	private static final Logger log = LoggerFactory.getLogger(KafkaDeliveryService.class);

	private Queue<KData> queue;
	private Queue<KData> inFlight;
	private boolean running;
	private String serverName;
	private int cnt;
	private KafkaAsyncService service;

	@Autowired
	public KafkaDeliveryService(KafkaAsyncService service) {
		super();
		this.service = service;
		this.queue = new ArrayDeque<>();
		this.inFlight = new ArrayDeque<>();
	}

	@Override
	public void addInit(String server) {
		this.serverName = server;
		cnt = 0;
	}

	@Override
	public void add(int nodeId, String apiKey, Iterator<Data> i) {
		while (i.hasNext()) {
			queue.add(new KData(serverName, apiKey, i.next()));
			cnt++;
		}
	}

	@Override
	public void addComplete() {
		if (cnt > 0) {
			cnt = 0;
		}
	}

	String getTopic(String apiKey) {
		return KafkaUtils.getTopic(serverName, apiKey);
	}

	@Override
	public void post() {
		// !!No thread-safe
		if (!running) {
			if (inFlight.isEmpty()) {
				inFlight.addAll(queue);
				queue.clear();
			}
			ListenableFuture<Map<String, Integer>> res = service.post(inFlight);
			res.addCallback(this);
		}
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

	@Override
	public boolean isEmpty() {
		return queue.isEmpty() && inFlight.isEmpty();
	}

}
