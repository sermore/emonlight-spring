package net.reliqs.emonlight.web.config;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.web.data.DataQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

import java.time.ZoneId;
import java.time.ZoneOffset;

@Configuration
public class WebConfig {

    @Autowired
    private SettingsService settingsService;

    @Bean
    public SettingsService settingsManager() {
        return new SettingsService();
    }

    @Bean
    @SessionScope
    public Settings settings() {
        Settings s = settingsService.load();
        return s;
    }

    @Bean
    public DataQueue dataQueue() {
        return new DataQueue();
    }

    @Bean
    public ZoneId serverZoneId() {
        return ZoneOffset.systemDefault();
    }

    //    @Bean
    //    @SessionScope
    //    public ZoneOffset clientOffset() {
    //        return serverZoneId().getRules().getof .from(serverZoneId());
    //    }

}
