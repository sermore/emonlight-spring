package net.reliqs.emonlight.web.config;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.SessionScope;

@Configuration
public class WebConfig {

    @Autowired
    private SettingsService settingsService;

    //    @Bean
    //    public SettingsService settingsservice() {
    //        return new SettingsService();
    //    }

    @Bean
    public ProbeMonitor monitor() {
        Settings s = settingsService.loadAndInitialize();
        return new ProbeMonitor(s);
    }

    @Bean
    @SessionScope
    public Settings settings() {
        Settings s = settingsService.load();
        return s;
    }


    //    @Bean
    //    public DataQueue dataQueue() {
    //        return new DataQueue();
    //    }

    //    @Bean
    //    @SessionScope
    //    public ZoneOffset clientOffset() {
    //        return serverZoneId().getRules().getof .from(serverZoneId());
    //    }

}
