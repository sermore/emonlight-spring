package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Settings;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, JpaConfiguration.class, Publisher.class})
@EnableAutoConfiguration
@EnableAsync
@ActiveProfiles("jpa")
public class JpaServiceTest {

    @Autowired
    private JpaNodeRepo nodeRepo;
    @Autowired
    private JpaProbeRepo probeRepo;
    @Autowired
    private JpaDataRepo dataRepo;

    public JpaProbe prepareData() {
        JpaNode n = new JpaNode();
        n.setName("test1");
        JpaProbe p = new JpaProbe();
        p.setName("p1");
        p.setNode(n);
        return p;
    }

    @Before
    public void clearData() {
//        dataRepo.deleteAll();
//        probeRepo.deleteAll();
        nodeRepo.deleteAll();
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


}
