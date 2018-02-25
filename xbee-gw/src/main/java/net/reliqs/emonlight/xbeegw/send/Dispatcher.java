package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class Dispatcher implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    @Value("${dispatcher.rate:5000}")
    private long rate;
    private Publisher publisher;
    @Value("${dispatcher.timeout:5000}")
    private long timeOut;
    private boolean running;

    public Dispatcher(Publisher publisher) {
        log.debug("init dispatcher");
        this.publisher = publisher;
        running = true;
    }

    /**
     * Deliver queued data.
     * No Thread-Safe, must be called in main thread.
     */
    public void process() {
            publisher.getServices().stream().filter(DeliveryService::isReady).forEach(DeliveryService::post);
    }

    public long getRate() {
        return rate;
    }

    /**
     * Try to clean up the services queues on termination.
     */
    @PreDestroy
    void cleanUp() {
        log.info("close");
    }

    @Override
    public int getPhase() {
        return 1000;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("clean up services queues, timeout {}", timeOut);
        Instant timeout = Instant.now().plus(timeOut, ChronoUnit.MILLIS);
        while (Instant.now().isBefore(timeout) && publisher.getServices().stream().anyMatch(s -> !s.isQueueEmpty())) {
            try {
                log.debug("waiting for services to complete");
                publisher.getServices().stream().filter(s -> !s.isQueueEmpty()).forEach(DeliveryService::post);
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn(e.getMessage());
            }
        }
        running = false;
        // continue shutdown process
        callback.run();
    }

    @Override
    public void start() {
        log.info("start");
    }

    @Override
    public void stop() {
        log.info("stop");
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
