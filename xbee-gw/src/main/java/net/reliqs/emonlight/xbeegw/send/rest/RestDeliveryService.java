package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Server;
import net.reliqs.emonlight.xbeegw.config.ServerMap;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class RestDeliveryService implements DeliveryService, ListenableFutureCallback<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(RestDeliveryService.class);

    private final Queue<RData> receiveQueue = new ArrayDeque<>();
    private final Queue<ServerDataJSON> queue = new ArrayDeque<>();
    private ServerDataJSON tempData = null;
    private ServerDataJSON inFlight;
    private ListenableFuture<Boolean> running;
    private int retryCount;
    private final Server server;
    private final Set<Probe> probes;
    private final RestAsyncService service;

    public RestDeliveryService(Server server, RestAsyncService service) {
        this.server = server;
        this.service = service;
        probes = new HashSet<>();
        this.server.getMaps().forEach(sm -> probes.add(sm.getProbe()));
    }

    RestTemplate getRestTemplate() {
        return service.getRestTemplate();
    }

    public Server getServer() {
        return server;
    }

    @Override
    public void receive(Probe p, Data d) {
        if (probes.contains(p)) {
            receiveQueue.add(new RData(p, d));
        }
    }

    @Override
    public boolean isReady() {
        return (inFlight != null || !queue.isEmpty() || !receiveQueue.isEmpty()) && (running == null || running.isDone());
    }

    ServerDataJSON pollReceiveQueue() {
        Queue<RData> inQueue = new ArrayDeque<>(receiveQueue);
	    receiveQueue.clear();
        ServerDataJSON sd = new ServerDataJSON();
        for (ServerMap sm : server.getMaps()) {
            NodeDataJSON nd = new NodeDataJSON(sm.getNodeId(), sm.getApiKey());
            inQueue.stream().filter(r -> r.probe.equals(sm.getProbe())).forEach(r -> {
                nd.addData(r.data);
                receiveQueue.remove(r);
            });
            if (!nd.getD().isEmpty()) {
                sd.getNodes().add(nd);
            }
        }
        if (!sd.getNodes().isEmpty()) {
            queue.add(sd);
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
                running = service.post(server.getUrl(), inFlight);
//				running = post(url, inFlight);
                running.addCallback(this);
            }
        }
    }

    @Override
    public void onSuccess(Boolean result) {
        log.debug("POST #{} OK : {} [{}]", retryCount, server.getUrl(), inFlight);
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
