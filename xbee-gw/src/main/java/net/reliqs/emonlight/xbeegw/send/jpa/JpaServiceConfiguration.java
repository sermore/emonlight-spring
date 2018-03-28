package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ConditionalOnProperty(name = "jpa.enabled")
@Configuration
@EnableJpaRepositories("net.reliqs.emonlight.xbeegw.send.jpa")
public class JpaServiceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(JpaServiceConfiguration.class);

    public JpaServiceConfiguration(Settings settings, JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo,
            JpaDataRepo dataRepo) {
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
    JpaAsyncService jpaAsyncService(Settings settings, JpaNodeRepo nodeRepo, JpaProbeRepo probeRepo,
            JpaDataRepo dataRepo, @Value("${jpa.maxRetries:0}") int maxRetries,
            @Value("${jpa.ignoreErrors:false}") boolean ignoreErrors) {
        return new JpaAsyncService(settings, nodeRepo, probeRepo, dataRepo, maxRetries, ignoreErrors);
    }

    @Bean(initMethod = "onInit", destroyMethod = "onClose")
    JpaService jpaService(Publisher publisher, JpaAsyncService asyncService,
            @Value("${jpa.enableBackup:true}") boolean enableBackup,
            @Value("${jpa.backupPath:jpaServiceBackup.dat}") String backupPath,
            @Value("${jpa.maxBatch:0}") int maxBatch, @Value("${jpa.realTime:false}") boolean realTime,
            @Value("${jpa.timeOutOnClose:2000}") long timeOutOnClose, @Value("${jpa.maxQueued:0}") int maxQueued) {
        JpaService service =
                new JpaService(asyncService, enableBackup, backupPath, maxBatch, realTime, timeOutOnClose, maxQueued);
        publisher.addService(service);
        return service;
    }

}
