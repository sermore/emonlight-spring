package net.reliqs.emonlight.xbeegw.send.jms;

import net.reliqs.emonlight.commons.data.StoreData;
import org.springframework.jms.annotation.JmsListener;

class Receiver {
    public static int count = 0;
    boolean received;

    @JmsListener(destination = "test")
    public void receiveMessage(StoreData data) {
        System.out.println("Received <" + data + ">");
        count++;
        received = true;
    }

}
