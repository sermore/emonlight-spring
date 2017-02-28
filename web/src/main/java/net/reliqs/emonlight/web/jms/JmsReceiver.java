package net.reliqs.emonlight.web.jms;

import net.reliqs.emonlight.commons.xbee.Data;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

/**
 * Created by sergio on 2/27/17.
 */
@Service
public class JmsReceiver {

    @JmsListener(destination = "0_TEST_PULSE")
    public void receiveMessage(Data data) {
        System.out.println("Received <" + data + ">");
    }
}
