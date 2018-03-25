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
public class RestV2ServiceTest {

    static private MockRestServiceServer mockServer;
    @Resource(name = "restV2_test1_asyncService")
    RestV2AsyncService asyncService;
    @Resource(name = "restV2_test1_service")
    RestV2Service service1;
    @Autowired
    Settings settings;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Files.deleteIfExists(Paths.get("test1_backup.dat"));
    }

    @AfterClass
    public static void afterClass() {
        mockServer.verify();
    }

    @Test
    @DirtiesContext
    public void test() throws InterruptedException {
        Server s = settings.getServers().stream().filter(ss -> ss.getName().equals("test1")).findFirst().get();
        assertThat(s, is(notNullValue()));
        Probe p = settings.getProbes().findFirst().get();

        assertThat(service1, is(notNullValue()));
        assertThat(asyncService.getUrl(), is(s.getUrl()));
        service1.setActive(true);

        mockServer = MockRestServiceServer.createServer(asyncService.getRestTemplate());
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withBadRequest());
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withUnauthorizedRequest());
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withNoContent());
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withServerError());
        mockServer.expect(requestTo("http://testserver1:1234/input/read.json")).andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

//        assertThat(service.isEmpty(), is(true));
        assertThat(service1.isReady(), is(false));
        Data in = new Data(100, 10.0);
        service1.receive(p, p.getType(), in);
        assertThat(service1.isReady(), is(true));
        in = new Data(200, 20.0);
        service1.receive(p, p.getType(), in);
        //        service.post();
        Thread.sleep(500);
        assertThat(service1.isReady(), is(false));
//        assertThat(service.isEmpty(), is(true));
        in = new Data(300, 30.0);
        service1.receive(p, p.getType(), in);
//        service.post();
//        Thread.sleep(1000);
//        assertThat(service.isReady(), is(true));
//        assertThat(service.isReady(), is(false));

//        mockServer.verify();
    }

}