package net.reliqs.emonlight.web.utils;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.services.JpaDataRepo;
import net.reliqs.emonlight.web.utils.DataGeneration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * Created by sergio on 25/02/17.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
//@SpringBootTest
@Transactional
@Import({JpaDataRepo.class, DataGeneration.class})
@ActiveProfiles({"testing"})
public class TestDataGeneration {

    @Autowired
    JpaDataRepo dataRepo;

    @Autowired
    DataGeneration dbUtils;

    @Test
    @Commit
    public void testDataGeneration() {
        Node n = dataRepo.findNode(1);
        assertThat(n, is(notNullValue()));
        dbUtils.generateConstantData(n, Instant.parse("2016-08-01T00:00:00.00Z"), Instant.parse("2016-08-10T00:00:00.00Z"), Duration.ofSeconds(120), 1200.0);
        dbUtils.generatePeriodicData(n, Instant.parse("2016-12-21T00:00:00.00Z"), Instant.parse("2017-01-10T00:00:00.00Z"), Duration.ofSeconds(600), 5000.0, Duration.ofDays(1));
    }

}
