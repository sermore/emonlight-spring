package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Created by sergio on 09/03/17.
 */
@ActiveProfiles("test-router")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, RestConfiguration.class, Publisher.class})
@EnableConfigurationProperties
@EnableAsync
public class RestDeliveryServiceTest {

    @Autowired
    RestAsyncService asyncService;

    @Autowired
    Settings settings;

    @Test
    public void test() throws InterruptedException {
        Server s = settings.getServers().get(0);
        Probe p = s.getMaps().get(0).getProbe();
        RestDeliveryService service = new RestDeliveryService(s, asyncService);
        assertThat(service.getServer(), is(s));

        MockRestServiceServer mockServer = MockRestServiceServer.createServer(service.getRestTemplate());
        mockServer.expect(requestTo("http://pino/emonlight-dev/input/read.json")).andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        mockServer.expect(requestTo("http://pino/emonlight-dev/input/read.json")).andRespond(withBadRequest());

//        assertThat(service.isEmpty(), is(true));
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
        Thread.sleep(1000);
        assertThat(service.isReady(), is(true));
//        assertThat(service.isEmpty(), is(false));

        mockServer.verify();
    }

}