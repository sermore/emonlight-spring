package net.reliqs.emonlight.web.config;

import net.reliqs.emonlight.commons.config.ISettings;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.web.git.FileRepositoryImpl;
import net.reliqs.emonlight.web.services.DataRepo;
import net.reliqs.emonlight.web.services.FileRepository;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    public ProbeMonitor monitor(DataRepo dataRepo, @Value("${historyDays:20}") int historyDays) {
        Settings s = settingsService.loadAndInitialize();
        return new ProbeMonitor(s, dataRepo, historyDays);
    }

    @Bean
    @SessionScope
    public ISettings settings() {
        Settings s = settingsService.load();
        return s;
    }

    @Bean(initMethod = "initRepo")
    public FileRepository fileRepository(@Value("${settings.path:settings.yml}") String path) {
        return new FileRepositoryImpl(path);
    }

    //    @Bean
    //    public CommonsRequestLoggingFilter requestLoggingFilter() {
    //        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
    //        loggingFilter.setIncludeClientInfo(true);
    //        loggingFilter.setIncludeQueryString(true);
    //        loggingFilter.setIncludePayload(true);
    //        return loggingFilter;
    //    }

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
