package net.reliqs.emonlight.xbeegw.config;

import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class})
@EnableConfigurationProperties
@ActiveProfiles("test-settings")
public class SettingsTest {

    @Autowired
    Settings s;

    @Test
    public void test() {
        assertNotNull(s);
        assertEquals("/dev/ttyUSB0", s.getSerialPort());
        assertEquals(2, s.getNodes().size());
        Node n = s.getNodes().get(0);
        assertEquals(500, n.getSampleTime());
        assertThat(n.getVccThreshold(), is(3.1));
        // assertNotNull(n.getState());
        assertEquals(4, n.getProbes().size());
        Probe p = n.getProbes().get(0);
        assertThat(p.getNode(), sameInstance(n));
        assertEquals("P1", p.getName());
        assertEquals(Type.PULSE, p.getType());
        // assertNotNull(p.getData());
        // assertSame(p, n.getProbe(Type.PULSE));
        Probe p2 = n.getProbes().get(1);
        assertEquals("P2", p2.getName());

        assertEquals(1, s.getServers().size());
        Server srv = s.getServers().get(0);
        assertEquals("S1", srv.getName());
        assertEquals(1, srv.getMaps().size());
        ServerMap sm = srv.getMaps().get(0);
        assertSame(n.getProbes().get(0), sm.getProbe());
        assertEquals(1, sm.getNodeId());
        assertEquals("12345", sm.getApiKey());
    }

    // @Test
    // public void testLoadFail() throws JsonParseException,
    // JsonMappingException, IOException {
    // thrown.expect(IllegalArgumentException.class);
    // thrown.expectMessage("nodes : node addresses not unique");
    // thrown.expectMessage("nodes : node names not unique");
    // thrown.expectMessage("mode PULSE_DHT22 requires at least 3 probes, one
    // PULSE and two for DHT22");
    // thrown.expectMessage("monitoring Vcc threshold requires a probe of type
    // VCC");
    // new Factory().load("src/test/resources/fail1.json");
    // }
    //
    // @Test
    // public void testJsonFail() throws JsonParseException,
    // JsonMappingException, IOException {
    // thrown.expect(UnresolvedForwardReference.class);
    // thrown.expectMessage("Unresolved forward references for:");
    // new Factory().load("src/test/resources/fail3.json");
    // }

}
