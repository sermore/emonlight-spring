package net.reliqs.emonlight.xbeegw.state;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Settings.class})
@EnableConfigurationProperties
@ActiveProfiles("test-settings")
public class GlobalStateTest {

    @Autowired
    private Settings settings;

    @Test
    public void test() {
        GlobalState g = new GlobalState(settings);
        assertThat(g.getNodeState("not-existing"), is(nullValue()));
        NodeState ns = g.getNodeState("X1");
        assertThat(ns.getNode(), is(sameInstance(settings.getNodes().get(0))));
    }

}