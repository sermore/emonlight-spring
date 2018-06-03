package net.reliqs.emonlight.xbeegw.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.DelayQueue;
import java.util.stream.Stream;

@Service
class EventQueue {
    private static final Logger log = LoggerFactory.getLogger(EventQueue.class);

    //    @Autowired
    //    private Processor processor;
    //    @Autowired
    //    private Dispatcher dispatcher;

    private DelayQueue<DelayedEvent> queue;

    public EventQueue() {
        this.queue = new DelayQueue<>();
    }

    /**
     * Queue initialization. Read DataMessage events from file and remove the file.
     */
    //    @PostConstruct
    //    void init() {
    //        if (backupEnabled) {
    //            CollectionStoreToFile<DelayedEvent> s = new CollectionStoreToFile<DelayedEvent>(backupPath);
    //            Collection<DelayedEvent> data = s.read(true);
    //            data.stream().forEach(e -> queue.offer(e));
    //            log.debug("read {} events from {}", data.size(), Paths.get(backupPath).toAbsolutePath());
    //        }
    //    }

    /**
     * Queue state backup on termination. Write queued DataMessage events to file.
     */
    //    @PreDestroy
    //    void close() {
    //        int qsize = queue.size();
    //        if (backupEnabled && qsize > 0) {
    //            CollectionStoreToFile<DelayedEvent> s = new CollectionStoreToFile<DelayedEvent>(backupPath);
    //            List<DelayedEvent> data = queue.stream().filter(DelayedEvent::isBackuppable).collect(Collectors.toList());
    //            s.write(data);
    //            log.debug("saved {} events to {}", data.size(), Paths.get(backupPath).toAbsolutePath());
    //        } else {
    //            if (qsize > 0) {
    //                log.warn("termination without backup and queue size = {}", qsize);
    //            } else {
    //                log.debug("termination with empty queue");
    //            }
    //        }
    //    }

    //    public boolean isBackupEnabled() {
    //        return backupEnabled;
    //    }
    //
    //    public void setBackupEnabled(boolean backupEnabled) {
    //        this.backupEnabled = backupEnabled;
    //    }
    //
    //    public String getBackupPath() {
    //        return backupPath;
    //    }
    //
    //    public void setBackupPath(String backupPath) {
    //        this.backupPath = backupPath;
    //    }

    boolean remove(DelayedEvent event) {
        log.trace("remove {}", event);
        return queue.remove(event);
    }

    void offer(DelayedEvent event) {
        log.trace("offer {}", event);
        queue.offer(event);
    }

    void reset(DelayedEvent event) {
        boolean res = remove(event);
        if (!res) {
            log.info("reset on event not in queue {}", event);
        }
        if (event.isScheduled()) {
            event.reset();
            offer(event);
        } else {
            log.info("event discarded {}", event);
        }
    }

    //    public void offerDataMessage(DataMessage dataMessage, long delay) {
    //        DelayedEvent event = new DelayedEvent(dataMessage, delay);
    //        log.trace("queue {}", event);
    //        queue.offer(event);
    //    }
    //
    //    public void offerDataMessage(DataMessage dataMessage) {
    //        offerDataMessage(dataMessage, 0L);
    //    }
    //
    //    public void offerStopEvent(long timeOut) {
    //        queue.offer(new DelayedEvent(Stop, timeOut));
    //    }
    //
    //    public void offerDispatcherEvent() {
    //        queue.offer(new DelayedEvent(DelayedEvent.EventType.Dispatcher, dispatcher.getRate()));
    //    }

    int run() {
        int retVal = 0;
        log.info("entering in event loop");
        //        queue.stream().forEach(DelayedEvent::reset);
        try {
            while (true) {
                DelayedEvent event = queue.take();
                log.trace("process {}", event);
                if (event == null || process(event))
                    break;
            }
        } catch (InterruptedException e) {
            log.error("event loop interrupted", e);
            retVal = 1;
        }
        log.info("exiting from event loop: {}", retVal);
        return retVal;
    }

    private boolean process(DelayedEvent event) {
        if (event.isScheduled()) {
            event.reset();
            offer(event);
        }
        return event.process();
        //            re
        //        switch (event.getEventType()) {
        //            case Stop:
        //                return true;
        //            case Message:
        //                processor.processDataMessage(event.getMsg());
        //                break;
        //            case Dispatcher:
        //                dispatcher.process();
        //                offerDispatcherEvent();
        //                break;
        //            default:
        //                throw new GwException("event type not found " + event);
        //        }
        //        return false;
    }

    void clear() {
        queue.clear();
    }

    int size() {
        return queue.size();
    }

    Stream<XbeeEvent> xbeeEvents() {
        return queue.stream().filter(e -> e instanceof XbeeEvent).map(e -> (XbeeEvent) e);
    }
}
