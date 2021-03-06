package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.data.Data;
import net.reliqs.emonlight.xbeegw.TestApp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@EnableCaching
@ActiveProfiles("integration,jpa")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    private void produceData(int cnt) throws InterruptedException {
        Random rng = new Random();
        for (int j = 0; j < cnt; j++) {
            for (int i = 0; i < 1000; i++) {
                int q = rng.nextInt(Long.valueOf(settings.getProbes().count()).intValue() - 1);
                Probe p = settings.getProbes().skip(q).findFirst().get();
                Data in = new Data(Instant.now().toEpochMilli(), rng.nextDouble() * 100.0);
                service.receive(p, p.getType(), in);
            }
            service.post();
            Thread.sleep(10);
        }
    }

    @Test
    public void checkIfJpaIsWorkingFine() {
        Probe probe = settings.getProbes().findFirst().get();
        JpaProbe p = probeRepo.findById(probe.getId());
        JpaData d = new JpaData(p, Instant.now(), 14.67F);
        dataRepo.save(d);
        assertThat(dataRepo.count(), is(1L));
        assertThat(nodeRepo.count(), is(1L));
    }

    @Test
    public void checkForInitialDbSetup() {
        assertThat(dataRepo.count(), is(0L));
        assertThat(nodeRepo.count(), is(1L));
        assertThat(probeRepo.count(), is(4L));
    }

    @Test
    public void checkForMillisecondInTimestamps() {
        Probe probe = settings.getProbes().findFirst().get();
        JpaProbe p = probeRepo.findById(probe.getId());
        JpaData d = new JpaData(p, Instant.ofEpochMilli(765), 14.67F);
        dataRepo.save(d);
        assertThat(dataRepo.findFirstByProbe(p).getTime(), equalTo(Timestamp.from(Instant.ofEpochMilli(765))));
    }

    @Test
    public void serviceTest() throws InterruptedException {
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

        assertThat(nodeRepo.findById(p.getNode().getId()), is(notNullValue()));
//        List<JpaProbe> probes = probeRepo.findByNodeAndName(nodes.get(0), p.getName());
//        assertThat(probes.size(), is(1  ));
//        assertThat(dataRepo.countByProbe(probes.get(0)), is(3L));
    }

    @Test(timeout = 10_000)
    public void checkForCaching() throws InterruptedException {
        produceData(20);
        while (!service.isQueueEmpty()) {
            service.post();
            Thread.sleep(500);
        }
        assertThat(service.isQueueEmpty(), is(true));
        assertThat(dataRepo.count(), is(20_000L));
    }

}
