package net.reliqs.emonlight.xbeegw.send.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import net.reliqs.emonlight.xbeegw.GwException;

/**
 * Created by sergio on 05/03/17.
 */
public class InfluxdbAsyncService {
    private static final Logger log = LoggerFactory.getLogger(InfluxdbAsyncService.class);

    private InfluxDB influxdb;

    public InfluxdbAsyncService(InfluxDB influxdb) {
        this.influxdb = influxdb;
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
        int cnt = inFlight.getPoints().size();
        if (cnt > 0) {
            influxdb.write(inFlight);
            log.trace("InfluxDB OK cnt={}", cnt);
        }
        AsyncResult<Integer> res = new AsyncResult<>(cnt);
        return res;
    }

}
