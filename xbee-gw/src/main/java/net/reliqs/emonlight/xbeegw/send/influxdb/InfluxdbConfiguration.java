package net.reliqs.emonlight.xbeegw.send.influxdb;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

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
    InfluxdbAsyncService influxdbAsyncService(@Value("${influxdb.url}") String influxdbUrl, @Value("${influxdb.database}") String dbName) {
        return new InfluxdbAsyncService(influxdbUrl, dbName);
    }

    @Bean
    @Order(10)
    InfluxdbService influxdbService(InfluxdbAsyncService service, @Value("${influxdb.database}") String dbName) {
        InfluxdbService s = new InfluxdbService(service, dbName);
        publisher.addSubscriber(s);
        return s;
    }
}
