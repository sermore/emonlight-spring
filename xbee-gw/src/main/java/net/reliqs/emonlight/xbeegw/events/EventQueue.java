package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.xbeegw.GwException;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.xbee.DataMessage;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.DelayQueue;

import static net.reliqs.emonlight.xbeegw.events.DelayedEvent.EventType.Stop;

@Service
public class EventQueue {
    private static final Logger log = LoggerFactory.getLogger(EventQueue.class);

    @Autowired
    private Processor processor;
    @Autowired
    private Dispatcher dispatcher;

    private DelayQueue<DelayedEvent> queue;

    public EventQueue() {
        this.queue = new DelayQueue<>();
//        this.processor = processor;
//        this.dispatcher = dispatcher;
    }

    public void offerDataMessage(DataMessage dataMessage, long delay) {
        DelayedEvent event = new DelayedEvent(dataMessage, delay);
        log.trace("queue {}", event);
        queue.offer(event);
    }

    public void offerDataMessage(DataMessage dataMessage) {
        offerDataMessage(dataMessage, 0L);
    }

    public void offerStopEvent(long timeOut) {
        queue.offer(new DelayedEvent(Stop, timeOut));
    }

    public void offerDispatcherEvent() {
        queue.offer(new DelayedEvent(DelayedEvent.EventType.Dispatcher, dispatcher.getRate()));
    }

    public int run(long timeOut) {
        if (timeOut > 0) {
            offerStopEvent(timeOut);
        }
        offerDispatcherEvent();
        try {
            while (true) {
                DelayedEvent event = queue.take();
                log.trace("process {}", event);
                if (event == null || process(event))
                    break;
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            return 1;
        }
        return 0;
    }

    boolean process(DelayedEvent event) {
        switch (event.getEventType()) {
            case Stop:
                return true;
            case Message:
                processor.processDataMessage(event.getMsg());
                break;
            case Dispatcher:
                dispatcher.process();
                offerDispatcherEvent();
                break;
            default:
                throw new GwException("event type not found " + event);
        }
        return false;
    }


}
