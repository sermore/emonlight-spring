package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.exceptions.XBeeException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import net.reliqs.emonlight.xbeegw.TestRouterConfig;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRouterConfig.class)
@ActiveProfiles("test-router")
public class RouterTest {

    @Autowired
    Settings settings;

    @Autowired
    Dispatcher t;

    @Autowired
    Processor p;

    @Test
    public void testRouter()
            throws XBeeException, JsonParseException, JsonMappingException, IOException, InterruptedException {
        assertThat(settings.getNodes().size(), is(1));
        assertThat(settings.getServers().size(), is(2));
        for (int i = 0; i < 40000; i++) {
            p.process();
            t.process();
        }
    }

}
