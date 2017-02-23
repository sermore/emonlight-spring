package net.reliqs.emonlight.streams;

import java.util.Arrays;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import net.reliqs.emonlight.streams.config.StreamsAppConfig;
import net.reliqs.emonlight.streams.streams.StreamProcess;

@SpringBootApplication
@EnableConfigurationProperties(StreamsAppConfig.class)
public class StreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamApplication.class, args);
	}

	@Autowired
	StreamsAppConfig config;

	@Bean
	StreamProcess streamProcess() {
		return new StreamProcess(config);
	}

	@Bean
	KafkaUtils kafkaUtils() {
		return new KafkaUtils();
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

			System.out.println("\nTopics:");
			for (Entry<String, String> e : config.getTopics().entrySet()) {
				System.out.println(e.getValue() + ": " + e.getKey());
			}
			streamProcess().start();

		};
	}

}
