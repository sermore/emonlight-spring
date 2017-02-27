package net.reliqs.emonlight.xbeegw;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.xbee.Processor;

class Runner {
	
	
	@Autowired
	Settings settings;

	@Autowired
	Processor processor;

	@Autowired
	Dispatcher dispatcher;

	Runner() {		
	}
	
	void run(ApplicationContext ctx) {

	    String[] beanNames = ctx.getBeanDefinitionNames();
	    Arrays.sort(beanNames);
	    for (String beanName : beanNames) {
	        System.out.println(beanName);
	    }

		System.out.printf("\n\nActive profiles: %s\n\n", Arrays.toString(ctx.getEnvironment().getActiveProfiles()));
		System.out.println("Nodes defined:");
		settings.getNodes().forEach(n -> System.out.println(" - " + n.getName()));
		System.out.println("\nServers defined:");
		settings.getServers().forEach(s -> System.out.println(" - " + s.getName()));
		System.out.println("\n\n\n");
		
		try {
			while(true) {
				processor.process();
				dispatcher.process();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (processor != null)
				processor.cleanup();
		}
	}

}
