package net.reliqs.emonlight.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Valid;

@Configuration
public class SettingsConfiguration {
    private static final Logger log = LoggerFactory.getLogger(SettingsConfiguration.class);


    private Settings load(String path) {
        Settings s = Settings.load(path == null || path.isEmpty() ? "settings.yml" : path);
        return s;
    }

    private Settings check(@Valid Settings s) {
        Settings.validate(s);
        return s;
    }

    @Bean
//    @ConfigurationProperties(prefix = "settings")
    public Settings settings(@Value("${settings.path:settings.yml}") String path) {
        return check(load(path));
    }

}
