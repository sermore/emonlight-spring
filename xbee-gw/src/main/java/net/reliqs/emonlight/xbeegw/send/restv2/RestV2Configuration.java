package net.reliqs.emonlight.xbeegw.send.restv2;

import net.reliqs.emonlight.commons.config.Server;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@ConditionalOnProperty(name = "restV2.enabled")
@Configuration
@Order(Integer.MAX_VALUE)
public class RestV2Configuration {
    private static final Logger log = LoggerFactory.getLogger(RestV2Configuration.class);

    @Autowired
    private ConfigurableApplicationContext ctx;

    private Settings settings;
    private Publisher publisher;
    @Value("${restV2.enableBackup:true}")
    private boolean enableBackup;
    @Value("${restV2.maxBatch:0}")
    private int maxBatch;
    @Value("${restV2.maxRetries:1}")
    private int maxRetries;

    public RestV2Configuration(Publisher publisher, Settings settings) {
        this.publisher = publisher;
        this.settings = settings;
    }

    RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        return restTemplate;
    }

    BeanDefinition createAsyncServiceBeanDefinition(String serverName, String url) {
        BeanDefinitionBuilder b = BeanDefinitionBuilder.rootBeanDefinition(RestV2AsyncService.class);
        b.addConstructorArgValue(serverName);
        b.addConstructorArgValue(maxRetries);
        b.addConstructorArgValue(createRestTemplate());
        b.addConstructorArgValue(url);
        return b.getBeanDefinition();
    }

    BeanDefinition createServiceBeanDefinition(String serviceName, Server server, boolean enableBackup, int maxBatch) {
        BeanDefinitionBuilder b =
                BeanDefinitionBuilder.rootBeanDefinition(RestV2Service.class).setInitMethodName("onInit")
                        .setDestroyMethodName("onClose");
        b.addConstructorArgReference(serviceName + "_asyncService");
        b.addConstructorArgValue(server.getName());
        b.addConstructorArgValue(enableBackup);
        b.addConstructorArgValue(server.getName() + "_backup.dat");
        b.addConstructorArgValue(maxBatch);
        b.addConstructorArgValue(server.getSendRate() == 0);
        b.addConstructorArgValue(Math.max(server.getSendRate() / 3, 2000));
        b.addConstructorArgValue(server.isActive());
        return b.getBeanDefinition();
    }

    @PostConstruct
    void init() {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) ctx.getBeanFactory();
        for (Server server : settings.getServers()) {
            if (server.getMaps() == null || server.getMaps().isEmpty()) {
                String serviceName = "restV2_" + server.getName();
                factory.registerBeanDefinition(serviceName + "_asyncService",
                        createAsyncServiceBeanDefinition(server.getName(), server.getUrl()));
                RestV2AsyncService asyncService = (RestV2AsyncService) ctx.getBean(serviceName + "_asyncService");
                assert asyncService != null;

                factory.registerBeanDefinition(serviceName + "_service",
                        createServiceBeanDefinition(serviceName, server, enableBackup, maxBatch));
                RestV2Service service = (RestV2Service) ctx.getBean(serviceName + "_service");
                assert service != null;

                log.debug("register: {} => {}", server, service);
                publisher.addService(service);
            }
        }
    }

}
