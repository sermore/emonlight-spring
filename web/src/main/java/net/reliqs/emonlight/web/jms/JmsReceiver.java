package net.reliqs.emonlight.web.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.time.Instant;

/**
 * Created by sergio on 2/27/17.
 */
@Service
public class JmsReceiver {
    private static final Logger log = LoggerFactory.getLogger(JmsReceiver.class);

    @JmsListener(destination = "0_TEST_PULSE")
    public void receiveMessage(BytesMessage msg) throws JMSException {
        log.debug("JSM: {}, {}, {}", msg, Instant.ofEpochMilli(msg.getJMSTimestamp()), msg.getPropertyNames());
    }
}
