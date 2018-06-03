package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.send.Dispatcher;
import net.reliqs.emonlight.xbeegw.state.CollectionStoreToFile;
import net.reliqs.emonlight.xbeegw.xbee.DataMessage;
import net.reliqs.emonlight.xbeegw.xbee.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventProcessorFacade {
    private static final Logger log = LoggerFactory.getLogger(EventProcessorFacade.class);

    private Processor processor;
    private Dispatcher dispatcher;
    private EventQueue queue;

    @Value("${eventQueue.backupEnabled:true}")
    private boolean backupEnabled;

    @Value("${eventQueue.backup:event-queue-backup.dat}")
    private String backupPath;

    public EventProcessorFacade(Dispatcher dispatcher, EventQueue queue) {
        //        this.processor = processor;
        this.dispatcher = dispatcher;
        this.queue = queue;
    }

    public void setProcessor(Processor processor) {
        Assert.state(processor != null, "set processor already called");
        this.processor = processor;
        init();
    }

    public boolean isBackupEnabled() {
        return backupEnabled;
    }

    public void setBackupEnabled(boolean backupEnabled) {
        this.backupEnabled = backupEnabled;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    /**
     * Queue initialization. Read DataMessage events from file and remove the file.
     */
    void init() {
        log.info("event processor initialization");
        Assert.state(processor != null, "processor not set");
        List<XbeeEvent> list = queue.xbeeEvents().collect(Collectors.toList());
        list.forEach(e -> {
            queue.remove(e);
            queueEvent(e);
        });
        if (backupEnabled) {
            CollectionStoreToFile<DataMessage> s = new CollectionStoreToFile<DataMessage>(backupPath);
            Collection<DataMessage> data = s.read(true);
            data.stream().forEach(msg -> queueMessage(msg, 0L));
            log.info("read {} messages from {}", data.size(), Paths.get(backupPath).toAbsolutePath());
        }
        startDispatcherPolling();
        log.info("event processor initialization completed");
    }

    /**
     * Queue state backup on termination. Write queued DataMessage events to file.
     */
    @PreDestroy
    void close() {
        int qsize = queue.size();
        if (backupEnabled && qsize > 0) {
            CollectionStoreToFile<DataMessage> s = new CollectionStoreToFile<DataMessage>(backupPath);
            List<DataMessage> data = queue.xbeeEvents().map(XbeeEvent::getMsg).collect(Collectors.toList());
            s.write(data);
            log.info("saved {} messages to {}", data.size(), Paths.get(backupPath).toAbsolutePath());
        } else {
            if (qsize > 0) {
                log.warn("termination without backup and queue size = {}", qsize);
            } else {
                log.debug("termination with empty queue");
            }
        }
    }

    public void queueEvent(DelayedEvent event) {
        queue.offer(event);
    }

    //    public void queueMessage(DataMessage msg, long delay) {
    //        queue.offer(new XbeeEvent(processor, msg, delay));
    //    }

    public void queueMessage(DataMessage msg, long delay) {
        queue.offer(new XbeeEvent(processor, msg, delay));
    }

    public void startDispatcherPolling() {
        queue.offer(new DispatcherEvent(dispatcher));
    }

    public void queueStopEvent(long time) {
        queue.offer(new StopEvent(time));
    }

    public void queueResetEvent() {
        queue.offer(new ResetEvent(processor));
    }

    public void queueThresholdAlarmEvent(Probe probe, int level) {
        queue.offer(new ThresholdAlarmEvent(processor, probe, level));
    }

    public void queueMissingAlarmEvent(Node node, int level) {
        queue.offer(new MissingAlarmEvent(processor, node));
    }

    public void eventReset(DelayedEvent event) {
        queue.reset(event);
    }

    public int run(long timeout) {
        if (timeout > 0) {
            queueStopEvent(timeout);
        }
        return queue.run();
    }
}
