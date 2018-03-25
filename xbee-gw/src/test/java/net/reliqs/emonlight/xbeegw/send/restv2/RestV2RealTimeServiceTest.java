package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.TestApp;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestApp.class})
@ActiveProfiles("integration,restv2")
public class RestV2RealTimeServiceTest {

    static private MockRestServiceServer mockServer;
    @Resource(name = "restV2_test2_service")
    RestV2Service service;
    @Resource(name = "restV2_test1_service")
    RestV2Service service1;
    @Autowired
    Settings settings;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Files.deleteIfExists(Paths.get("test2_backup.dat"));
    }

    @AfterClass
    public static void afterClass() {
        mockServer.verify();
    }

    @Test
    @DirtiesContext
    public void test() throws InterruptedException {
        service1.setActive(false);
        service.setActive(true);
        Server s = settings.getServers().stream().filter(ss -> ss.getName().equals("test2")).findFirst().get();
        assertThat(s, is(notNullValue()));
        Probe p = settings.getProbes().findFirst().get();
        assertThat(service, is(notNullValue()));

        mockServer = MockRestServiceServer.createServer(service.getService().getRestTemplate());
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json"))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json"))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json")).andRespond(withBadRequest());
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json")).andRespond(withUnauthorizedRequest());
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json")).andRespond(withNoContent());
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json")).andRespond(withServerError());
        mockServer.expect(requestTo("http://testserver2:1234/input/read.json"))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        //        assertThat(service.isEmpty(), is(true));
        assertThat(service.isReady(), is(false));
        Data in = new Data(100, 10.0);
        service.receive(p, p.getType(), in);
        Thread.sleep(100);
        assertThat(service.isReady(), is(false));
        in = new Data(200, 20.0);
        service.receive(p, p.getType(), in);
        Thread.sleep(100);
        assertThat(service.isReady(), is(false));
        //        assertThat(service.isEmpty(), is(true));
        in = new Data(300, 30.0);
        service.receive(p, p.getType(), in);
        //        service.post();
        //        Thread.sleep(1000);
        //        assertThat(service.isReady(), is(true));
        //        assertThat(service.isReady(), is(false));

        //        mockServer.verify();
    }

}