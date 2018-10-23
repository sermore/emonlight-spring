package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.commons.config.*;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.data.Data;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by sergio on 26/02/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SettingsConfiguration.class, SettingsService.class})
@EnableConfigurationProperties
@ActiveProfiles("test-router")
public class TriggerLevelTest {

    @Autowired
    private
    Settings settings;

    private int level;
    private int levelChangeCnt;

    @Test
    public void test() {
        Probe p = settings.getNodes().get(0).getProbes().get(0);
        TriggerLevel t = new TriggerLevel(p, null, TriggerLevel.powerTriggers(p));
        t.addHandler(new TriggerHandler() {
            @Override
            public void triggerChanged(Probe probe, Type type, int oldValue, int newValue) {
                level = newValue;
                levelChangeCnt++;
            }

            @Override
            public void triggerDataAbsentChanged(Node node, Type type, int oldValue, int newValue) {

            }
        });

        t.process(p, new Data(0, 0));
        assertThat(level, is(0));
        t.process(p, new Data(1000, 4000));
        assertThat(level, is(4));
        assertThat(levelChangeCnt, is(1));
        t.process(p, new Data(2000, 3999));
        assertThat(level, is(1));
        assertThat(levelChangeCnt, is(2));
        t.process(p, new Data(3000, 3200));
        assertThat(level, is(0));
        assertThat(levelChangeCnt, is(3));
        t.process(p, new Data(3_500_000, 3300));
        assertThat(level, is(1));
        assertThat(levelChangeCnt, is(4));
        t.process(p, new Data(3_600_000, 3300));
        assertThat(level, is(2));
        assertThat(levelChangeCnt, is(5));
        t.process(p, new Data(3_601_000, 3200));
        assertThat(level, is(0));
        assertThat(levelChangeCnt, is(6));
        t.process(p, new Data(7_190_000, 3300));
        assertThat(level, is(2));
        assertThat(levelChangeCnt, is(7));
        t.process(p, new Data(7_200_000, 3300));
        assertThat(level, is(3));
        assertThat(levelChangeCnt, is(8));
        t.process(p, new Data(7_200_100, 4000));
        assertThat(level, is(4));
        assertThat(levelChangeCnt, is(9));
    }

}