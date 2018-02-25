package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConditionalOnProperty(name = "jpa.enabled")
@Configuration
@EnableJpaRepositories("net.reliqs.emonlight.xbeegw.send.jpa")
public class JpaServiceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JpaServiceConfiguration.class);

    public JpaServiceConfiguration(Settings settings, JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo, JpaDataRepo dataRepo) {
        setupDb(settings, nodeRepo, probeRepo);
    }

    void setupDb(Settings settings, JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo) {
        settings.getNodes().forEach(n -> {
            JpaNode node = nodeRepo.createNodeIfNotExists(probeRepo, n);
//            log.debug("JPA: node saved {}", node);
        });
        log.debug("JPA: database setup completed");
    }

    @Bean
    JpaAsyncService jpaAsyncService(JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo, JpaDataRepo dataRepo) {
        return new JpaAsyncService(nodeRepo, probeRepo, dataRepo);
    }

    @Bean(initMethod = "onInit", destroyMethod = "onClose")
    JpaService jpaService(Publisher publisher, JpaAsyncService asyncService) {
        JpaService service = new JpaService(asyncService);
        publisher.addService(service);
        return service;
    }

}
