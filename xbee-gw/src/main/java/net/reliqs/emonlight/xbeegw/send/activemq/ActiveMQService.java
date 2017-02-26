package net.reliqs.emonlight.xbeegw.send.activemq;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.JmsTemplate;

import java.util.Iterator;

/**
 * Created by sergio on 25/02/17.
 */
public class ActiveMQService implements DeliveryService {

    private JmsTemplate jmsTemplate;
    private JmsOperations jmsOperations;

    @Override
    public void post() {

    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void receive(Probe p, Data d) {

    }
}
