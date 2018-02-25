package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.events.EventQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApp.class)
@ActiveProfiles({"integration", "test-router"})
public class RouterTest {

    @Autowired
    private Settings settings;

    @Autowired
    private EventQueue queue;

    @Test
    public void testRouter() {
        assertThat(settings.getNodes().size(), is(1));
        assertThat(settings.getServers().size(), is(2));
        queue.run(5000L);
    }

}
