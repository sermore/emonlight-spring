package net.reliqs.emonlight.commons.config;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SettingsConfiguration.class})
@EnableConfigurationProperties
@ActiveProfiles({"test-settings"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SettingsTest {

    @Autowired
    private Settings s;

    private static Validator validator;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterClass
    public static void afterClass() throws IOException {
        Files.copy(Paths.get("src/test/resources/settings.yml.bak"), Paths.get("src/test/resources/settings.yml"), REPLACE_EXISTING);
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
    public void testDumpAndLoad() {
        s.setBaudRate(200);
        assertThat(s.dump("src/test/resources/settings.yml"), is(true));
        Settings s1 = Settings.load("src/test/resources/settings.yml");
        assertThat(s1.getBaudRate(), is(200));
        assertEquals(2, s.getNodes().size());

        assertThat(Settings.load("not_existing"), is(nullValue()));
        assertThat(s.dump("src/test/resources/"), is(false));
    }

    @Test
    public void testForValidate() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Settings invalid: nodes[0].id -> may not be null");
        s.getNodes().get(0).setId(null);
        Settings.validate(s);
    }

    @Test
    public void testForValidation() {
        s.getNodes().get(0).setSampleTime(0);
        s.getNodes().get(0).setId(0);
        s.getProbes().iterator().next().setId(null);
        Set<ConstraintViolation<Settings>> res = validator.validate(s);
        assertThat(res, hasSize(3));
        assertThat(res.stream().map(c -> String.format("%s -> %s", c.getPropertyPath(), c.getMessage())).collect(Collectors.toList()),
                containsInAnyOrder("nodes[0].sampleTime -> must be greater than or equal to 1",
                        "nodes[0].id -> must be greater than or equal to 1",
                        "nodes[0].probes[0].id -> may not be null"));
    }
}
