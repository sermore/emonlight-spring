package net.reliqs.emonlight.xbeegw.send.rest;

import java.util.ArrayDeque;
import java.util.Queue;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.config.ServerMap;
import net.reliqs.emonlight.xbeegw.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;

public class RestDeliveryService implements DeliveryService, ListenableFutureCallback<Boolean> {
	private static final Logger log = LoggerFactory.getLogger(RestDeliveryService.class);

	private final Queue<RData> receiveQueue = new ArrayDeque<>();
	private ServerDataJSON tempData = null;
	private final Queue<ServerDataJSON> queue = new ArrayDeque<>();
	private ServerDataJSON inFlight;
	private ListenableFuture<Boolean> running;
	private int retryCount;
    private Settings settings;
    private RestAsyncService service;
	private String url;

	@Autowired
	public RestDeliveryService(Settings settings, RestAsyncService service) {
        this.settings = settings;
        this.service = service;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void receive(Probe p, Data d) {
        receiveQueue.add(new RData(p, d));
	}

	@Override
	public boolean isReady() {
		return (inFlight != null || !queue.isEmpty() || !receiveQueue.isEmpty()) && (running == null || running.isDone());
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	ServerDataJSON pollReceiveQueue() {
	    Queue<RData> inQueue = new ArrayDeque<>(receiveQueue);
	    receiveQueue.clear();
	    for(Server s : settings.getServers()) {
            ServerDataJSON sd = new ServerDataJSON();
	        for(ServerMap sm : s.getMaps()) {
	            NodeDataJSON nd = new NodeDataJSON(sm.getNodeId(), sm.getApiKey());
                inQueue.stream().filter(r -> r.probe.equals(sm.getProbe())).forEach(r -> nd.addData(r.data));
	            if (!nd.getD().isEmpty()) {
                    sd.getNodes().add(nd);
                }
            }
            if (!sd.getNodes().isEmpty()) {
	            queue.add(sd);
            }
        }
	    return queue.poll();
	}

	@Override
	public void post() {
		if (isReady() && running == null) {
			if (inFlight == null)
				inFlight = pollReceiveQueue();
			if (inFlight != null) {
				retryCount++;
//				log.debug("PRE POST #{} : {} [{}]", retryCount, url, inFlight);
				running = service.post(url, inFlight);
//				running = post(url, inFlight);
				running.addCallback(this);
			}
		}
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
