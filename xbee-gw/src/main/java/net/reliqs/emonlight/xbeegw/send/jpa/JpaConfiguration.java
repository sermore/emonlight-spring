package net.reliqs.emonlight.xbeegw.send.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ConditionalOnProperty(name = "jpa.enabled", matchIfMissing = true, havingValue = "")
@EnableJpaRepositories("net.reliqs.emonlight.xbeegw.send.jpa")
@EnableTransactionManagement
public class JpaConfiguration {
}
