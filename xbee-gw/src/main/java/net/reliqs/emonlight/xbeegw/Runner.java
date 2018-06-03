package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.events.EventProcessorFacade;
import org.springframework.beans.factory.annotation.Autowired;

public class Runner {

    @Autowired
    Settings settings;
    //    @Autowired
//    Processor processor;
//    @Autowired
//    Dispatcher dispatcher;
    @Autowired
    EventProcessorFacade eventProcessorFacade;

    public Runner() {
    }

    public void run(long timeOut) {
        System.out.println("Nodes defined:");
        settings.getNodes().forEach(n -> System.out.println(" - " + n.getName()));
        System.out.println("\nServers defined:");
        settings.getServers().forEach(s -> System.out.println(" - " + s.getName()));
        System.out.println("\n\n\n");

        eventProcessorFacade.run(timeOut);
    }

}
