package net.reliqs.emonlight.xbeegw.send.influxdb;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, InfluxdbConfiguration.class, Publisher.class})
@EnableAutoConfiguration
@EnableAsync
@ActiveProfiles("test-router")
public class InfluxdbServiceTest {

    @Autowired
    private InfluxDB influxDB;

    @Value("${timezone:}")
    private String timezone;

    @Test
    public void test() throws IOException {
        String measurement = "zigbee2";
        ZoneId zone = timezone != null && !timezone.isEmpty() ? ZoneId.of(timezone) : ZoneId.systemDefault();
        Instant start = Instant.now().minus(5, ChronoUnit.MINUTES);
        BufferedWriter writer = Files.newBufferedWriter(Paths.get("out.txt"), Charset.forName("UTF-8"));
        QueryResult res;
        String lastTime = null;
        do {
            Query q = new Query("select * from zigbee where time > '" + start.toString() + "' order by time", "emonlight");
            res = influxDB.query(q);
            lastTime = null;
            for (Result r : res.getResults()) {
                if (r.getSeries() != null) {
                    for (QueryResult.Series s : r.getSeries()) {
                        System.out.println("TAGS=" + s.getTags());
                        System.out.println("COLS=" + s.getColumns());
                        Map<String, Integer> tagMap = tagsMap(s.getColumns());
                        for (List<Object> lk : s.getValues()) {
                            System.out.println("V=" + lk);
                            writer.write(lineProtocol(tagMap, s.getColumns(), lk, measurement, zone));
                            lastTime = (String) lk.get(0);
                        }
                    }
                }
            }
            if (lastTime != null) {
                start = Instant.parse(lastTime);
                System.out.println("START=" + start);
            }
        } while (lastTime != null);
        writer.close();
    }

    Map<String, Integer> tagsMap(List<String> cols) {
        Map<String, Integer> m = new HashMap<>();
        m.put("address", cols.indexOf("address"));
        m.put("node", cols.indexOf("node"));
        m.put("probe", cols.indexOf("probe"));
        return m;
    }

    String lineProtocol(Map<String, Integer> tm, List<String> cols, List<Object> v, String measurement, ZoneId zone) {
        Instant t = Instant.parse((CharSequence) v.get(0));
        ZonedDateTime tz = ZonedDateTime.ofInstant(t, zone);
        String fieldName = null;
        Double fieldValue = null;
        List<String> vals = new ArrayList<>();
        for (int i = 1; i < v.size(); i++) {
            Object val = v.get(i);
            if (val != null) {
                vals.add(cols.get(i) + "=" + val);
                break;
            }
        }
        String l = String.format("%s,address=%s,node=%s,probe=%s,hour=%d,dayOfWeek=%d,month=%d,year=%d %s %d\n",
                measurement,
                v.get(tm.get("address")),
                v.get(tm.get("node")),
                v.get(tm.get("probe")),
                tz.getHour(),
                tz.getDayOfWeek().ordinal(),
                tz.getMonthValue(),
                tz.getYear(),
                String.join(",", vals),
                t.toEpochMilli());
        return l;
    }

}
