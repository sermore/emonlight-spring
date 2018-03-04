package net.reliqs.emonlight.xbeegw.state;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.StoreData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class})
@EnableConfigurationProperties
@ActiveProfiles("test-settings")
public class ObjStoreToFileTest {

    @Autowired
    private Settings settings;

    @Test
    public void test() {
        ObjStoreToFile<ArrayList<? extends Serializable>> ss = new ObjStoreToFile<>("test.dat", true);
        ArrayList<StoreData> q1 = new ArrayList<>();
        ArrayList<StoreData> q2 = new ArrayList<>();
        Probe p = settings.getProbes().findFirst().get();
        q1.add(new StoreData(p, Probe.Type.PULSE, new Data(100L, 120.4)));
        q1.add(new StoreData(p, Probe.Type.PULSE, new Data(110L, 130.4)));
        p = settings.getProbes().filter(pp -> pp.getName().equals("P2")).findFirst().get();
        q2.add(new StoreData(p, Probe.Type.PULSE, new Data(120L, 140.4)));
        q2.add(new StoreData(p, Probe.Type.PULSE, new Data(130L, 150.4)));
        assertThat(ss.isEmpty(), is(true));
        ss.add(q1);
        assertThat(ss.isEmpty(), is(false));
        ss.add(q2);
        assertThat(ss.write(), is(true));
        assertThat(Files.exists(Paths.get("test.dat")), is(true));
        List<ArrayList<? extends Serializable>> res = ss.read();
        assertThat(Files.exists(Paths.get("test.dat")), is(false));
        assertThat(res.get(0).get(0), equalTo(q1.get(0)));
        assertThat(res.get(0), equalTo(q1));
        assertThat(res.get(1), equalTo(q2));
        assertThat(res, equalTo(Arrays.asList(q1, q2)));
    }
}