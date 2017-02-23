package net.reliqs.emonlight.xbeegw;

import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import net.reliqs.emonlight.xbeegw.MainApp;

// @SpringBootApplication(scanBasePackageClasses = { Settings.class,
// Processor.class})
@Profile("test-router")
// @SpringBootApplication(scanBasePackages = { "net.reliqs.emonlight.xbeegw" },
// exclude = { MainApp.class })
@SpringBootApplication(exclude = { MainApp.class })
@EnableAsync
public class TestRouterConfig extends AsyncConfigurerSupport {

	@Override
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(2);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("Dispatch-");
		executor.initialize();
		return executor;
	}
}