package net.reliqs.emonlight.xbeegw.send.rest;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.xbee.Data;

public class RestDeliveryService implements DeliveryService, ListenableFutureCallback<Boolean> {
	private static final Logger log = LoggerFactory.getLogger(RestDeliveryService.class);

	private ServerDataJSON tempData = null;
	private final Queue<ServerDataJSON> queue = new ArrayDeque<>();
	private ServerDataJSON inFlight;
	private ListenableFuture<Boolean> running;
	private int retryCount;
	private RestAsyncService service;
	private String url;

	@Autowired
	public RestDeliveryService(RestAsyncService service) {
		this.service = service;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void addInit(String server) {
		tempData = new ServerDataJSON();
	}

	@Override
	public void add(int nodeId, String apiKey, Iterator<Data> i) {
		NodeDataJSON nd = new NodeDataJSON(nodeId, apiKey);
		while (i.hasNext()) {
			nd.addData(i.next());
		}
		if (!nd.getD().isEmpty())
			tempData.getNodes().add(nd);
	}

	@Override
	public void addComplete() {
		if (!tempData.getNodes().isEmpty()) {
			queue.add(tempData);
		}
		tempData = null;
	}

	@Override
	public boolean isReady() {
		return (inFlight != null || !queue.isEmpty()) && (running == null || running.isDone());
	}

	ServerDataJSON pollQueue() {
		return queue.poll();
	}

	@Override
	public void post() {
		if (isReady() && running == null) {
			if (inFlight == null)
				inFlight = pollQueue();
			if (inFlight != null) {
				retryCount++;
//				log.debug("PRE POST #{} : {} [{}]", retryCount, url, inFlight);
				running = service.post(url, inFlight);
//				running = post(url, inFlight);
				running.addCallback(this);
			}
		}
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

//	@Async
//	ListenableFuture<Boolean> post(String url, ServerDataJSON sd) {
//		String res;
//		boolean ok;
//		log.debug("REST {} -> {}",sd, url);
//		res = restTemplate.postForObject(url, sd, String.class);
//		log.debug("REST {} <- {}", res, url);
//		ok = "OK".equals(res);
//		return new AsyncResult<>(ok);
//	}

	@Override
	public void onSuccess(Boolean result) {
		log.debug("POST #{} OK : {} [{}]", retryCount, url, inFlight);
		running = null;
		inFlight = null;
		retryCount = 0;
	}

	@Override
	public void onFailure(Throwable ex) {
		log.warn("FAIL #{} {} : {}", retryCount, ex.getMessage(), inFlight);
		running = null;
	}

	@Override
	public String toString() {
		return String.format(" [queue=%d, inFlight=%s, retryCount=%s]", queue.size(), inFlight != null, retryCount);
	}

}
