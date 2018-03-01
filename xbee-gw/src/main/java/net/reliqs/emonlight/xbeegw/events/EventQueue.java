package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.xbeegw.GwException;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.state.CollectionStoreToFile;
import net.reliqs.emonlight.xbeegw.xbee.DataMessage;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.reliqs.emonlight.xbeegw.events.DelayedEvent.EventType.Stop;

@Service
public class EventQueue {
    private static final Logger log = LoggerFactory.getLogger(EventQueue.class);

    @Autowired
    private Processor processor;
    @Autowired
    private Dispatcher dispatcher;

    private DelayQueue<DelayedEvent> queue;

    @Value("${eventQueue.backup:event-queue-backup.dat}")
    private String backupPath;

    public EventQueue() {
        this.queue = new DelayQueue<>();
    }

    /**
     * Queue initialization. Read DataMessage events from file and remove the file.
     */
    @PostConstruct
    void init() {
        CollectionStoreToFile<DelayedEvent> s = new CollectionStoreToFile<DelayedEvent>(backupPath);
        Collection<DelayedEvent> data = s.read(true);
        data.forEach(e -> offerDataMessage(e.getMsg(), e.getDelay(TimeUnit.MILLISECONDS)));
        log.debug("read {} events from {}", data.size(), backupPath);
    }

    /**
     * Queue state backup on termination. Write queued DataMessage events to file.
     */
    @PreDestroy
    void close() {
        if (queue.size() > 0) {
            CollectionStoreToFile<DelayedEvent> s = new CollectionStoreToFile<DelayedEvent>(backupPath);
            List<DelayedEvent> data = queue.stream().filter(e -> e.getEventType() == DelayedEvent.EventType.Message).collect(Collectors.toList());
            s.write(data);
            log.debug("saved {} events to {}", data.size(), backupPath);
        } else {
            log.debug("queue empty on close");
        }
    }

    public String getBackupPath() {
        return backupPath;
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

    public void clear() {
        queue.clear();
    }

    public long size() {
        return queue.size();
    }
}
