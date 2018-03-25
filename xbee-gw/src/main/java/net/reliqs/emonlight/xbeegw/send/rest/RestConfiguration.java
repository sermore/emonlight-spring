package net.reliqs.emonlight.xbeegw.send.rest;

import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@ConditionalOnProperty(name = "rest.enabled")
@Configuration
public class RestConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RestConfiguration.class);

    private Settings settings;
    private RestTemplateBuilder rb;
    private Publisher publisher;

    @Autowired
    public RestConfiguration(Publisher publisher, Settings settings) {
        this.publisher = publisher;
        this.rb = new RestTemplateBuilder();
        this.settings = settings;
    }

    @Bean
    @Scope("prototype")
    RestAsyncService restAsyncService() {
        return new RestAsyncService(rb);
    }

    RestDeliveryService restDeliveryService(Server server) {
        RestDeliveryService s = new RestDeliveryService(server, restAsyncService());
        log.debug("register: {} => {}", server, s);
        publisher.addService(s);
        return s;
    }

    @PostConstruct
    void init() {
        settings.getServers().stream().filter(ss -> !ss.getMaps().isEmpty()).forEach(ss -> {
            RestDeliveryService svc = restDeliveryService(ss);
        });
    }

}
