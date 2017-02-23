package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.Sample;
import net.reliqs.emonlight.web.utils.DbUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertThat;

/**
 * Created by sergio on 19/02/17.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({JpaDataRepo.class, DbUtils.class})
public class JpaDataRepoTest {

    @Autowired
    JpaDataRepo dataRepo;

    @Autowired
    DbUtils dbUtils;

    @Test
    public void test() throws Exception {
        Node n = new Node();
        n.setTimeZone("UTC");
        n.setTitle("N1");
        n.setAuthenticationToken("XXN1");
        n = dataRepo.saveNode(n);

        Node n1 = dataRepo.findNode(n.getId());
        assertThat(n1, is(n));

        n1 = dataRepo.findNodeByAuthenticationToken("XXN1");
        assertThat(n1, is(n));

        List<Sample> ls = new ArrayList<Sample>();
        Sample s = new Sample();
        Instant t = Instant.now();
        s.setNode(n1);
        s.setSampleTime(new Timestamp(t.minus(5, ChronoUnit.MINUTES).toEpochMilli()));
        s.setValue(23.5);
        ls.add(s);
        s = new Sample();
        s.setNode(n1);
        s.setSampleTime(new Timestamp(t.toEpochMilli()));
        s.setValue(43.5);
        ls.add(s);
        s = new Sample();
        s.setNode(n1);
        Instant t1 = t.plus(1, ChronoUnit.SECONDS).truncatedTo(ChronoUnit.SECONDS).plus(1, ChronoUnit.MILLIS);
        s.setSampleTime(new Timestamp(t1.toEpochMilli()));
        s.setValue(43.5);
        ls.add(s);
        dataRepo.saveSamples(ls);

        List<Sample> ls1 = dataRepo.getSamples(n.getId(), new Timestamp(t.toEpochMilli()));
        assertThat(ls1, hasSize(1));
        Sample s1 = ls1.get(0);
        assertThat(s1, is(s));
//        assertThat(ls1.get(0).getSampleTime(), is(t1));

    }

    @Test
    public void testGetData() {
        List<Node> nn = dbUtils.persistNodes();
        Instant end = Instant.now().plus(5, ChronoUnit.HOURS);
        Instant start = end.minus(30, ChronoUnit.DAYS);
        dbUtils.saveSampleData(nn.get(0), start, end);

        Iterable<Number[]> l = dataRepo.getData(Arrays.asList(nn.get(0).getId()), Instant.now().toEpochMilli());
        assertThat(l, iterableWithSize(1));
    }


}