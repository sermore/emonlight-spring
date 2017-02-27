package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.commons.kafka.utils.KafkaUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Profile({ "default", "prod", "dev" })
@SpringBootApplication
@Import(KafkaUtils.class)
@EnableConfigurationProperties
@EnableAsync
public class MainApp extends AsyncConfigurerSupport {

	public static void main(String[] args) {
		SpringApplication.run(MainApp.class, args);
	}

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("Worker-");
		executor.initialize();
		return executor;
	}

	@Bean
	Runner runner() {
		return new Runner();
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			runner().run(ctx);
		};
	}

	// @Bean
	// Runner runner() {
	// return new Runner();
	// };

	// public static void main(String[] args) {
	// Processor processor = null;
	// try {
	// String conf = args.length == 0 ? "conf.json" : args[0];
	// Settings settings = new Factory().load(conf);
	// processor = new Processor(settings);
	// Dispatcher dispatcher = new Dispatcher(settings);
	// for (int i = 0; true; i++) {
	// processor.process();
	// if (i % 10 == 9)
	// dispatcher.process();
	// }
	// } catch (IOException | XBeeException | InterruptedException e) {
	// e.printStackTrace();
	// } finally {
	// if (processor != null)
	// processor.cleanup();
	// }
	// }

}
