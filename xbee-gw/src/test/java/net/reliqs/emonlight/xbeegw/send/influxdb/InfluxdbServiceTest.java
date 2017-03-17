package net.reliqs.emonlight.xbeegw.send.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, InfluxdbConfiguration.class, Publisher.class})
@EnableAutoConfiguration
@EnableAsync
@ActiveProfiles("jms")
public class InfluxdbServiceTest {

    @Autowired
    private InfluxDB influxDB;
    
    @Test
    public void test() {
        QueryResult query = influxDB.query(null);
        for (Result r : query.getResults()) {
        }
    }

}
