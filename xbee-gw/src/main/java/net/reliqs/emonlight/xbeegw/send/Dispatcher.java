package net.reliqs.emonlight.xbeegw.send;

import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class Dispatcher implements SmartLifecycle {
    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    @Value("${dispatcher.rate:5000}")
    private long rate;
    private Publisher publisher;

    @Value("${dispatcher.timeout:15000}")
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

    /**
     * Try to clean up the services queues on termination.
     */
    @PreDestroy
    void cleanUp() {
        log.info("close");
    }

    @Override
    public int getPhase() {
        return 100;
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("clean up services queues, timeout {}, queue {}", timeOut, publisher.getServices().size());
        int retry = timeOut / 10 >= 500 ? 10 : (int) timeOut / 500;
        long timeOutStep = timeOut / retry;
        for (; retry > 0 && publisher.getServices().stream().anyMatch(s -> !s.isQueueEmpty()); retry--) {
            try {
                log.debug("#{}: call post on each busy services", retry);
                publisher.getServices().stream().filter(s -> !s.isQueueEmpty()).forEach(DeliveryService::post);
                log.debug("#{}: waiting {} ms for services to complete", retry, timeOutStep);
                Thread.sleep(timeOutStep);
            } catch (InterruptedException e) {
                log.warn("failure while waiting for services to complete", e);
            }
        }
        running = false;
        long queue = publisher.getServices().stream().filter(s -> !s.isQueueEmpty()).count();
        if (queue > 0) {
            log.warn("proceed with termination having {} services with pending queue", queue);
        } else {
            log.debug("all services are idle, proceed with termination");
        }
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

    public long getTimeOut() {
        return timeOut;
    }

    void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public long getRate() {
        return rate;
    }

    void setRate(long rate) {
        this.rate = rate;
    }
}
