package net.reliqs.emonlight.commons.config;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.SerializationUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SettingsService.class, SettingsConfiguration.class})
@EnableConfigurationProperties
@ActiveProfiles({"test-settings"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SettingsTest {

    @Autowired
    private Settings s;

    private static Validator validator;

    @BeforeClass
    public static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void test() {
        assertNotNull(s);
        assertEquals("/dev/ttyUSB0", s.getSerialPort());
        assertThat(s.getBaudRate(), is(115200));
        assertEquals(2, s.getNodes().size());
        Node n = s.getNodes().get(0);
        assertThat(n.getId(), is(1));
        assertEquals(500, n.getSampleTime());
        assertThat(n.getVccThreshold(), is(3.1));
        // assertNotNull(n.getState());
        assertEquals(4, n.getProbes().size());
        Probe p = n.getProbes().get(0);
        assertThat(p.getNode(), sameInstance(n));
        assertEquals("P1", p.getName());
        assertEquals(Probe.Type.PULSE, p.getType());
        // assertNotNull(p.getData());
        // assertSame(p, n.findProbeByTypeAndPort(Type.PULSE));
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

    @Test
    public void testFindProbe() {
        Node n = s.getNodes().get(0);
        assertThat(n.findProbeByName("P2"), is(notNullValue()));
        assertThat(n.findProbeByName("P2").getName(), is("P2"));
        assertThat(n.findProbeByTypeAndPort(Probe.Type.DHT22_H, (byte) 10), is(n.findProbeByName("P2")));
    }

    @Test
    public void testForValidation() {
        s.setSerialPort("");
        s.getNodes().get(0).setSampleTime(0);
        s.getNodes().get(0).setId(0);
        s.getProbes().iterator().next().setId(null);
        Set<ConstraintViolation<Settings>> res = validator.validate(s);
        assertThat(res, hasSize(4));
        assertThat(res.stream().map(c -> String.format("%s -> %s", c.getPropertyPath(), c.getMessage())).collect(Collectors.toList()),
                containsInAnyOrder("serialPort -> size must be between 4 and 2147483647",
                        "nodes[0].sampleTime -> must be greater than or equal to 1",
                        "nodes[0].id -> must be greater than or equal to 1",
                        "nodes[0].probes[0].id -> may not be null"));
    }

    @Test
    public void serializationTest() throws IOException, ClassNotFoundException {
        byte[] b = SerializationUtils.serialize(s);
        assertThat(b, is(notNullValue()));
        Settings s1 = (Settings) SerializationUtils.deserialize(b);
        //        assertThat(s, equalTo(s1));
        assertThat(s1, is(notNullValue()));
        FileOutputStream out = new FileOutputStream("test.txt");
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(s);
        out.close();
        FileInputStream in = new FileInputStream("test.txt");
        ObjectInputStream is = new ObjectInputStream(in);
        Settings s2 = (Settings) is.readObject();
        assertThat(s2, is(notNullValue()));
    }
}
