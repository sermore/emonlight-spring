package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@ConditionalOnProperty(name = "restV2.enabled")
@Configuration
public class RestV2Configuration {
    private static final Logger log = LoggerFactory.getLogger(RestV2Configuration.class);

    @Autowired
    private ConfigurableApplicationContext ctx;

    private Settings settings;
    private RestTemplateBuilder rb;
    private Publisher publisher;
    @Value("${restV2.enableBackup:true}")
    private boolean enableBackup;
    @Value("${restV2.maxBatch:0}")
    private int maxBatch;
    @Value("${restV2.maxRetries:1}")
    private int maxRetries;
    private Server server;
    private RestV2AsyncService asyncService;

    public RestV2Configuration(Publisher publisher, Settings settings) {
        this.publisher = publisher;
        this.rb = new RestTemplateBuilder();
        this.settings = settings;
    }

    //    @Bean(name = "${this.serviceName}")
//    @Scope("prototype")
    RestV2AsyncService createAsyncService(String url) {
        return new RestV2AsyncService(rb, url, maxRetries);
    }

//    @Bean(name="${this.serviceName}", initMethod = "onInit", destroyMethod = "onClose")
//    @Scope("prototype")
//    RestV2Service createRestService(Server server, RestV2AsyncService asyncService) {
//        RestV2Service s = new RestV2Service(asyncService, server.getName(), enableBackup,
//                server.getName() + "_backup.dat", maxBatch, server.getSendRate() == 0, Math.max(server.getSendRate() / 3, 2000));
//        log.debug("register: {} => {}", server, s);
//        publisher.addService(s);
//        return s;
//    }

    RestV2Service createRestService() {
        RestV2Service s = new RestV2Service(asyncService, server.getName(), enableBackup,
                server.getName() + "_backup.dat", maxBatch, server.getSendRate() == 0, Math.max(server.getSendRate() / 3, 2000));
        log.debug("register: {} => {}", server, s);
        publisher.addService(s);
        return s;
    }

    @PostConstruct
    void init() {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RestV2Service.class);
        builder.setInitMethodName("onInit").setDestroyMethodName("onClose");
        builder.setFactoryMethodOnBean("createRestService", "restV2Configuration");
//        builder = BeanDefinitionBuilder.rootBeanDefinition(RestV2AsyncService.class);
        for (Server s : settings.getServers()) {
            server = s;
            if (server.getMaps() == null || server.getMaps().isEmpty()) {
                String serviceName = "restV2_" + server.getName();
                asyncService = createAsyncService(server.getUrl());
                ctx.getBeanFactory().registerSingleton(serviceName + "_asyncService", asyncService);
                assert ctx.getBean(serviceName + "_asyncService") != null;

//                builder. addConstructorArgValue(asyncService);
//                builder.addConstructorArgValue(s.getName());
//                builder.addConstructorArgValue(enableBackup);
//                builder.addConstructorArgValue(s.getName() + "_backup.dat");
//                builder.addConstructorArgValue(maxBatch);
//                builder.addConstructorArgValue(s.getSendRate() == 0);
//                builder.addConstructorArgValue(Math.max(s.getSendRate() / 3, 2000));
//                RestV2Service service = createRestService(s, asyncService);
                ((DefaultListableBeanFactory) ctx.getBeanFactory()).registerBeanDefinition(serviceName + "_service", builder.getBeanDefinition());
//                ctx.getBeanFactory().registerSingleton("restV2_" + s.getName() + "_service", service);
                RestV2Service service = (RestV2Service) ctx.getBean(serviceName + "_service");
                assert service != null;
//                log.debug("register: {} => {}", s, service);
//                publisher.addService(service);
            }
        }
    }

}
