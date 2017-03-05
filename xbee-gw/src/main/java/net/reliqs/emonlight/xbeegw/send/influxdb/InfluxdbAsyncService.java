package net.reliqs.emonlight.xbeegw.send.influxdb;

import net.reliqs.emonlight.xbeegw.GwException;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sergio on 05/03/17.
 */
@Component
public class InfluxdbAsyncService {
    private static final Logger log = LoggerFactory.getLogger(InfluxdbAsyncService.class);

    private final String influxdbUrl;
    private final String dbName;
    private InfluxDB influxdb;

    public InfluxdbAsyncService(@Value("${influxdb.url}") String influxdbUrl, @Value("${influxdb.database}") String dbName) {
        this.influxdbUrl = influxdbUrl;
        this.dbName = dbName;
        influxdb = InfluxDBFactory.connect(this.influxdbUrl);
        Pong p = influxdb.ping();
        if (p == null) {
            influxdb.close();
            influxdb = null;
            throw new GwException("database not available");
        }
    }

    @Async
    @Transactional
    public ListenableFuture<Integer> post(BatchPoints inFlight) {
        Map<String, Integer> counters = new HashMap<String, Integer>();
        int cnt = inFlight.getPoints().size();
        if (cnt > 0) {
            influxdb.write(inFlight);
            log.trace("InfluxDB OK cnt={}", cnt);
        }
        AsyncResult<Integer> res = new AsyncResult<>(cnt);
        return res;
    }

    @PreDestroy
    public void close() {
        if (influxdb != null) {
            influxdb.close();
        }
    }

}
