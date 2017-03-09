package net.reliqs.emonlight.xbeegw.send.influxdb;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.TimeUnit;

/**
 * Created by sergio on 05/03/17.
 */
public class InfluxdbService implements DeliveryService, ListenableFutureCallback<Integer> {
    private static final Logger log = LoggerFactory.getLogger(InfluxdbService.class);

    private BatchPoints queue, inFlight;
    private boolean running;
    private InfluxdbAsyncService service;
    private String dbName;

    @Autowired
    public InfluxdbService(InfluxdbAsyncService service, String dbName) {
        this.service = service;
        this.dbName = dbName;
        queue = createBatchPoints();
    }

    BatchPoints createBatchPoints() {
        return BatchPoints.database(this.dbName).retentionPolicy("autogen").consistency(InfluxDB.ConsistencyLevel.ALL).build();
    }

    @Override
    public void receive(Probe p, Data d) {
        Point point = Point.measurement("zigbee").tag("node", p.getNode().getName())
                .tag("address", p.getNode().getAddress()).tag("probe", p.getName()).addField(p.getType().name(), d.v).time(d.t, TimeUnit.MILLISECONDS).build();
        queue.point(point);
    }

    @Override
    public void post() {
        if (!running) {
            running = true;
            if (inFlight == null) {
                inFlight = queue;
                queue = createBatchPoints();
            }
            ListenableFuture<Integer> res = service.post(inFlight);
            res.addCallback(this);
        }
    }

    @Override
    public boolean isReady() {
        return !running && (inFlight != null || !queue.getPoints().isEmpty());
    }

    @Override
    public void onFailure(Throwable ex) {
        running = false;
        log.warn("Influxdb FAIL q={}, inFlight={}: {}", queue.getPoints().size(), inFlight.getPoints().size(), ex.getMessage());
    }

    @Override
    public void onSuccess(Integer result) {
        log.debug("Influxdb q={}, {}", queue.getPoints().size(), result);
        running = false;
        inFlight = null;
    }
}
