package net.reliqs.emonlight.web.services;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by sergio on 19/02/17.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
//@AutoConfigureTestDatabase(replace = NONE)
//@SpringBootTest
@Transactional
//@Import({JpaDataRepo.class})
//@ActiveProfiles({"testing"})
public class JpaDataRepoTest {

    @Autowired
    DataRepo dataRepo;

    @Ignore
    @Test
    public void test() throws Exception {
//        Node n = new Node();
//        n.setTimeZone("UTC");
//        n.setTitle("N1");
//        n.setAuthenticationToken("YYN1");
//        n = dataRepo.saveNode(n);

//        Node n1 = dataRepo.findNode(n.getId());
//        assertThat(n1, is(n));
//
//        n1 = dataRepo.findNodeByAuthenticationToken("YYN1");
//        assertThat(n1, is(n));
//
//        List<Sample> ls = new ArrayList<Sample>();
//        Sample s = new Sample();
//        Instant t = Instant.now();
//        s.setNode(n1);
//        s.setSampleTime(new Timestamp(t.minus(5, ChronoUnit.MINUTES).toEpochMilli()));
//        s.setValue(23.5);
//        ls.add(s);
//        s = new Sample();
//        s.setNode(n1);
//        s.setSampleTime(new Timestamp(t.toEpochMilli()));
//        s.setValue(43.5);
//        ls.add(s);
//        s = new Sample();
//        s.setNode(n1);
//        Instant t1 = t.plus(1, ChronoUnit.SECONDS).truncatedTo(ChronoUnit.SECONDS).plus(1, ChronoUnit.MILLIS);
//        s.setSampleTime(new Timestamp(t1.toEpochMilli()));
//        s.setValue(43.5);
//        ls.add(s);
//        dataRepo.saveSamples(ls);
//
//        List<Sample> ls1 = dataRepo.getSamples(n.getId(), new Timestamp(t.toEpochMilli()));
//        assertThat(ls1, hasSize(1));
//        Sample s1 = ls1.get(0);
//        assertThat(s1, is(s));
//        assertThat(ls1.get(0).getSampleTime(), is(t1));

    }

}