package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.commons.xbee.Data;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.ServerMap;
import net.reliqs.emonlight.xbeegw.config.Settings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


@ActiveProfiles("test-router")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class, RestConfiguration.class})
@EnableConfigurationProperties
@EnableAsync
public class RestDeliveryServiceTest {

    @Autowired
    private Settings settings;

    @Autowired
    private RestAsyncService ra;

    @Autowired
    private RestDeliveryService service;

    @Test
    public void testAdd() {
        ServerMap sm = settings.getServers().get(0).getMaps().get(0);
        Probe p = sm.getProbe();

        ServerDataJSON sd = new ServerDataJSON();
        NodeDataJSON nd = new NodeDataJSON(sm.getNodeId(), sm.getApiKey());
        sd.getNodes().add(nd);

        assertThat(service.isReady(), is(false));

        long t = 0;
        Data in = new Data(t, 0.0);
        nd.addData(in);
        service.receive(p, in);
        assertThat(service.isReady(), is(true));

        t += 1000;
        in = new Data(t, 100.0);
        nd.addData(in);
        service.receive(p, in);

        t += 1000;
        in = new Data(t, 140.0);
        nd.addData(in);
        service.receive(p, in);

        assertThat(service.pollReceiveQueue(), is(sd));

//        service.post();
//        assertThat(service.isReady(), is(false));
//
//        Thread.sleep(2000);
//
//        assertThat(service.isReady(), is(true));

    }

    @Test
    public void testPost() throws InterruptedException, ExecutionException {
        // RestDeliveryService rest1 = new RestDeliveryService(ra,
        // "http://pino/emonlight-dev/input/read.json");
        //
        // RestDeliveryService rest2 = new RestDeliveryService(ra,
        // "http://acero/emonlight-dev/input/read.json");
        //
        // assertThat(rest1, is(not(rest2)));

        Data in = new Data(Instant.now().toEpochMilli(), 199);
        NodeDataJSON nd = new NodeDataJSON(15, "mvXvsisjD_V9ze9iVokf");
        nd.addData(in);
        ServerDataJSON sd = new ServerDataJSON();
        sd.getNodes().add(nd);

        Future<Boolean> res = ra.post("http://pino/emonlight-dev/input/read.json", sd);
        assertThat(res.get(), is(true));

        res = ra.post("http://pino/emonlight-dev/input/read.json", new ServerDataJSON());
        try {
            res.get();
            fail();
        } catch (Exception e) {
            assertThat(e, instanceOf(ExecutionException.class));
        }

    }

}
