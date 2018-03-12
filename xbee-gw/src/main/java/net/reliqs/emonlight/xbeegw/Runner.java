package net.reliqs.emonlight.xbeegw;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.events.EventQueue;
import org.springframework.beans.factory.annotation.Autowired;

public class Runner {

    @Autowired
    Settings settings;
    //    @Autowired
//    Processor processor;
//    @Autowired
//    Dispatcher dispatcher;
    @Autowired
    EventQueue queue;

    public Runner() {
    }

    public void run(long timeOut) {
        System.out.println("Nodes defined:");
        settings.getNodes().forEach(n -> System.out.println(" - " + n.getName()));
        System.out.println("\nServers defined:");
        settings.getServers().forEach(s -> System.out.println(" - " + s.getName()));
        System.out.println("\n\n\n");

        queue.run(timeOut);
    }

}
