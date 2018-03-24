package net.reliqs.emonlight.commons.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsConfiguration {

    @Autowired
    private SettingsService settingsService;

    @Bean
    public Settings settings() {
        return settingsService.load();
    }


}
