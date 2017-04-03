package net.reliqs.emonlight.xbeegw.send.influxdb;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;

/**
 * Created by sergio on 05/03/17.
 */
public class InfluxdbService implements DeliveryService, ListenableFutureCallback<Integer> {
    private static final Logger log = LoggerFactory.getLogger(InfluxdbService.class);

    private BatchPoints queue, inFlight;
    private boolean running;
    private InfluxdbAsyncService service;
    private String dbName;
    private String measurement;
    private ZoneId zoneId;

    public InfluxdbService(InfluxdbAsyncService service, String dbName, String measurement, String timezone) {
        this.service = service;
        this.dbName = dbName;
        this.measurement = measurement;
        this.zoneId = timezone != null && !timezone.isEmpty() ? ZoneId.of(timezone) : ZoneId.systemDefault();
        queue = createBatchPoints();
    }

    BatchPoints createBatchPoints() {
        return BatchPoints.database(dbName).retentionPolicy("autogen").consistency(InfluxDB.ConsistencyLevel.ALL).build();
    }

    @Override
    public void receive(Probe p, Type type, Data d) {
        ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(d.t), zoneId);
        Point point = Point.measurement(measurement)
                .tag("node", p.getNode().getName())
                .tag("address", p.getNode().getAddress())
                .tag("probe", p.getName())
                .tag("hour", String.valueOf(t.getHour()))
                .tag("dayOfWeek", String.valueOf(t.getDayOfWeek().ordinal()))
                .tag("month", String.valueOf(t.getMonthValue()))
                .tag("year", String.valueOf(t.getYear()))
                .addField(type.name(), d.v).time(d.t, TimeUnit.MILLISECONDS).build();
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
