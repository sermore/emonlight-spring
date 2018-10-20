package net.reliqs.emonlight.web;

import net.reliqs.emonlight.commons.config.SettingsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
@SpringBootApplication
@EnableScheduling
//@EnableWebMvc
@Import({SettingsService.class})
@EnableAutoConfiguration(exclude = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        JmsAutoConfiguration.class, KafkaAutoConfiguration.class, ThymeleafAutoConfiguration.class})
//@EnableJpaRepositories(basePackages = "net.reliqs.emonlight.web.entities")
//@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
//@ActiveProfiles({ "dev", "prod" })
public class WebApp {

    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            //            System.out.println("Let's inspect the beans provided by Spring Boot:");
            //
            //            String[] beanNames = ctx.getBeanDefinitionNames();
            //            Arrays.sort(beanNames);
            //            for (String beanName : beanNames) {
            //                System.out.println(beanName);
            //            }

        };
    }

}
