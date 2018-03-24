package net.reliqs.emonlight.commons.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ConvertFromApplicationSettingsTest.MyConfiguration.class})
@EnableConfigurationProperties
@ActiveProfiles({"test-settings", "settings"})
public class ConvertFromApplicationSettingsTest {

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private Settings settings;

    @Test
    public void saveToSettings() {
        assertThat(settingsService.dump(settings, "settings.yml"), is(true));
    }

    @SpringBootConfiguration
    static class MyConfiguration {

        @Bean
        @ConfigurationProperties(prefix = "settings")
        public Settings settings() {
            Settings s = new Settings();
            return s;
        }

    }

}
