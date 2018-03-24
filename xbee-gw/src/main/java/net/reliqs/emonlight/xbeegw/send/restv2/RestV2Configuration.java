package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@ConditionalOnProperty(name = "restV2.enabled")
@Configuration
public class RestV2Configuration {
    private static final Logger log = LoggerFactory.getLogger(RestV2Configuration.class);

    private Settings settings;
    private RestTemplateBuilder rb;
    private Publisher publisher;
    @Value("${restV2.enableBackup:true")
    private boolean enableBackup;
    @Value("${restV2.maxBatch:0")
    private int maxBatch;
    @Value("${restV2.maxRetries:1")
    private int maxRetries;

    @Autowired
    public RestV2Configuration(Publisher publisher, Settings settings) {
        this.publisher = publisher;
        this.rb = new RestTemplateBuilder();
        this.settings = settings;
    }

    @Bean
    @Scope("prototype")
    RestV2AsyncService restV2AsyncService(String url) {
        return new RestV2AsyncService(rb, url, maxRetries);
    }

    @Bean(initMethod = "onInit", destroyMethod = "onClose")
    @Scope("prototype")
    RestV2Service createRestService(Server server) {
        RestV2Service s = new RestV2Service(restV2AsyncService(server.getUrl()), server.getName(), enableBackup,
                server.getName() + "_backup.dat", maxBatch, server.getSendRate() == 0, server.getSendRate() / 3);
        log.debug("register: {} => {}", server, s);
        publisher.addService(s);
        return s;
    }

    @PostConstruct
    void init() {
        for (Server s : settings.getServers()) {
            if (s.getMaps() == null || s.getMaps().isEmpty()) {
                createRestService(s);
            }
        }
    }

}
