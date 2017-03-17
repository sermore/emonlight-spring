package net.reliqs.emonlight.xbeegw.send.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.reliqs.emonlight.xbeegw.publish.Publisher;

/**
 * Created by sergio on 08/03/17.
 */
@Configuration
@ConditionalOnProperty(name = "influxdb.enabled", matchIfMissing = true, havingValue = "")
public class InfluxdbConfiguration {

    private Publisher publisher;

    @Autowired
    public InfluxdbConfiguration(Publisher publisher) {
        this.publisher = publisher;
    }

    @Bean
    InfluxdbAsyncService influxdbAsyncService(InfluxDB influxdb) {
        return new InfluxdbAsyncService(influxdb);
    }
    
    @Bean(destroyMethod="close")
    InfluxDB influxDB(@Value("${influxdb.url}") String influxdbUrl, @Value("${influxdb.database}") String dbName) {
        return InfluxDBFactory.connect(influxdbUrl);
    }

    @Bean
    InfluxdbService influxdbService(InfluxdbAsyncService service, @Value("${influxdb.database}") String dbName, @Value("${influxdb.measurement}") String measurement) {
        InfluxdbService s = new InfluxdbService(service, dbName, measurement);
        publisher.addService(s);
        return s;
    }
}
