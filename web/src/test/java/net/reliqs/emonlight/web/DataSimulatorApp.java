package net.reliqs.emonlight.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

//@AutoConfigureTestDatabase(replace = NONE)
//@SpringBootApplication(exclude = { App.class }, scanBasePackages = { "net.reliqs.emonlight.web" })
@SpringBootApplication(scanBasePackages = {"net.reliqs.emonlight.web.entities"})
@EnableJpaRepositories(basePackages = "net.reliqs.emonlight.web.entities")
@EnableAutoConfiguration(exclude = WebMvcAutoConfiguration.class)
public class DataSimulatorApp {

    public static void main(String[] args) {
        SpringApplication.run(DataSimulatorApp.class, args);
    }

    @Bean
    DataSimulator dataSim() {
        return new DataSimulator(5, 5000);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
            dataSim().run();
        };
    }

}
