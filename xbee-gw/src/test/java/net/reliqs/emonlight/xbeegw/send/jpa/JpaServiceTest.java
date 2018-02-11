package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, JpaConfiguration.class, Publisher.class})
@EnableAutoConfiguration
@EnableAsync
//@EnableCaching
@ActiveProfiles("jpa")
public class JpaServiceTest {

    @Autowired
    Settings settings;
    @Autowired
    private JpaNodeRepo nodeRepo;
    @Autowired
    private JpaProbeRepo probeRepo;
    @Autowired
    private JpaDataRepo dataRepo;
    @Autowired
    private JpaService service;
    @Autowired
    private JpaConfiguration configuration;

    public JpaProbe prepareData() {
        JpaNode n = new JpaNode();
        n.setName("test1");
        nodeRepo.save(n);
        JpaProbe p = new JpaProbe();
        p.setName("p1");
        p.setNode(n);
        probeRepo.save(p);
        return p;
    }

    @Before
    public void clearData() {
        dataRepo.deleteAll();
        probeRepo.deleteAll();
        nodeRepo.deleteAll();
        dataRepo.flush();
    }

    @Test
    public void checkIfJpaIsWorkingFine() {
        JpaProbe p = prepareData();
        JpaData d = new JpaData(p, Instant.now(), 14.67F);
        dataRepo.save(d);
        assertThat(dataRepo.count(), is(1L));
        dataRepo.deleteAll();
        assertThat(nodeRepo.count(), is(1L));
    }

    @Test
    public void checkForMillisecondInTimestamps() {
        JpaProbe p = prepareData();
        JpaData d = new JpaData(p, Instant.ofEpochMilli(765), 14.67F);
        dataRepo.save(d);
        assertThat(dataRepo.findFirstByProbe(p).getTime(), equalTo(Timestamp.from(Instant.ofEpochMilli(765))));
    }

    @Test
    public void serviceTest() throws InterruptedException {
        configuration.setupDb(settings, nodeRepo, probeRepo);

        Probe p = settings.getProbes().findFirst().get();

        assertThat(service.isReady(), is(false));
        Data in = new Data(100, 10.0);
        service.receive(p, p.getType(), in);
        assertThat(service.isReady(), is(true));
        in = new Data(200, 20.0);
        service.receive(p, p.getType(), in);
        service.post();
        Thread.sleep(1000);
        assertThat(service.isReady(), is(false));
//        assertThat(service.isEmpty(), is(true));
        in = new Data(300, 30.0);
        service.receive(p, p.getType(), in);
        service.post();
        assertThat(service.isReady(), is(false));
        Thread.sleep(1000);
        assertThat(service.isReady(), is(false));

        assertThat(nodeRepo.countByName(p.getNode().getName()), is(1L));
        assertThat(nodeRepo.findByName(p.getNode().getName()), is(notNullValue()));
//        List<JpaProbe> probes = probeRepo.findByNodeAndName(nodes.get(0), p.getName());
//        assertThat(probes.size(), is(1  ));
//        assertThat(dataRepo.countByProbe(probes.get(0)), is(3L));
    }

    @Test
    public void checkForCaching() throws InterruptedException {
        configuration.setupDb(settings, nodeRepo, probeRepo);

        Random rng = new Random();
        for (int j = 0; j < 50; j++) {
            for (int i = 0; i < 1000; i++) {
                int q = rng.nextInt(Long.valueOf(settings.getProbes().count()).intValue() - 1);
                Probe p = settings.getProbes().skip(q).findFirst().get();
                Data in = new Data(Instant.now().toEpochMilli(), rng.nextDouble() * 100.0);
                service.receive(p, p.getType(), in);
            }
            service.post();
            Thread.sleep(10);
        }
        while (!service.isQueueEmpty()) {
            service.post();
            Thread.sleep(1000);
        }
        assertThat(service.isQueueEmpty(), is(true));
        assertThat(dataRepo.count(), is(50_000L));
//        Thread.sleep(2000);
    }

}
