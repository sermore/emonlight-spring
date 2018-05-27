package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsConfiguration;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.xbeegw.events.EventQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Profile({"default", "prod", "dev"})
@SpringBootApplication
@Import({SettingsService.class, SettingsConfiguration.class})
@EnableConfigurationProperties()
@EnableAutoConfiguration(
        exclude = {JmsAutoConfiguration.class,
                KafkaAutoConfiguration.class, WebMvcAutoConfiguration.class})
@EnableAsync
@EnableScheduling
@EnableCaching
public class MainApp extends AsyncConfigurerSupport {

    public static void main(String[] args) {
        SpringApplication.run(MainApp.class, args);
    }

    @Autowired
    Settings settings;

//    @Bean
//    Runner runner() {
//        return new Runner();
//    }

    @Autowired
    EventQueue queue;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

        //        String[] beanNames = ctx.getBeanDefinitionNames();
        //        Arrays.sort(beanNames);
        //        for (String beanName : beanNames) {
        //            System.out.println(beanName);
        //        }

        System.out.printf("\n\nActive profiles: %s\n\n", Arrays.toString(ctx.getEnvironment().getActiveProfiles()));

        System.out.println("Nodes defined:");
        settings.getNodes().forEach(n -> System.out.println(" - " + n.getName()));
        System.out.println("\nServers defined:");
        settings.getServers().forEach(s -> System.out.println(" - " + s.getName()));
        System.out.println("\n\n\n");

        return args -> {
//            runner().run(0L);
            queue.run(0);
        };
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
