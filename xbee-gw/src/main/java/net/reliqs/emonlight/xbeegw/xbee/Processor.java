package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.utils.HexUtils;
import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerManager;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Processor of the xbee events.
 * <p>
 * Handle a queue containing the messages received from remote xbee devices. The
 * processing of every message could produce a response message to be sent to
 * the xbee, or/and produce a data output to be published. The details of the
 * message processing is handled by specific MessageProcessor instances,
 * selected using the first byte of the received message.
 */
@Component
public class Processor {
    private static final Logger log = LoggerFactory.getLogger(Processor.class);

    private final BlockingQueue<DataMessage> queue;
    private final Map<Byte, MessageProcessor> procs;
    private final XbeeGateway gateway;
    private final GlobalState globalState;
    private final Publisher publisher;
    //    @Value("${processor.timeout:1000}")
//    private long timeout;
    @Value("${processor.maxProcessTime:1000}")
    private long maxProcessTime;

    @Autowired
    public Processor(final Settings settings, final XbeeGateway gateway, final GlobalState globalState,
                     final Publisher publisher, final TriggerManager triggerManager) throws XBeeException {
        this.gateway = gateway;
        this.globalState = globalState;
        this.publisher = publisher;
        queue = new LinkedBlockingQueue<>();
        procs = new HashMap<>();
        procs.put((byte) 'C', new ConfigurationProcessor(this));
        procs.put((byte) 'J', new DHT22Processor(this));
        procs.put((byte) 'S', new DS18B20Processor(this));
        procs.put((byte) 'V', new VCCProcessor(this));
        procs.put((byte) 'P', new PulseProcessor(this, globalState));
        procs.put((byte) 'D', new MultiDataProcessor(this));
        procs.put((byte) 'H', procs.get((byte) 'J'));
        procs.put((byte) 'W', procs.get((byte) 'V'));
        procs.put((byte) 'B', procs.get((byte) 'S'));
        gateway.setProcessor(this);
        settings.getNodes().forEach(n -> registerNode(triggerManager, n));
//        triggerManager.registerTriggerDataAbsent(handler);
        log.debug("processor configuration complete");
    }

    private void registerNode(TriggerManager triggerManager, Node n) {
        log.debug("setup node {}", n);
        String addr = n.getAddress();
        NodeState ns = globalState.getNodeState(addr);
        ns.setDevice(gateway.addDevice(addr));
        n.getProbes().forEach(p -> {
            if (p.hasThresholds()) {
                PulseProcessor pp = (PulseProcessor) procs.get((byte) 'P');
                triggerManager.createTriggerLevel(ns, p, pp);
            }
        });
    }

    void sendData(NodeState ns, byte[] data) {
        log.debug("{}: send {}", ns.getNode(), HexUtils.byteArrayToHexString(data));
        gateway.sendDataAsync(ns.getDevice(), data);
    }

    void queue(final DataMessage msg) {
        queue.offer(msg);
    }

    public void process() throws InterruptedException {
        Instant processTime = Instant.now().plus(maxProcessTime, ChronoUnit.MILLIS);
        do {
            DataMessage m = queue.poll(maxProcessTime / 4, TimeUnit.MILLISECONDS);
            if (m != null) {
                processDataMessage(m);
            } else
                return;
        } while (Instant.now().isBefore(processTime));
    }

    private void processDataMessage(DataMessage m) {
        NodeState ns = globalState.getNodeState(m.getDeviceAddress());
        if (ns != null) {
            ByteBuffer in = ByteBuffer.wrap(m.getData());
            processContent(m, ns, in);
        } else {
            log.warn("message {} discarded", m);
        }
    }

    private void processContent(DataMessage m, NodeState nodeState, ByteBuffer in) {
        while (in.hasRemaining()) {
            byte b = in.get();
            MessageProcessor mp = procs.get(b);
            if (mp != null) {
                try {
                    mp.process(m, nodeState, b, in);
                } catch (BufferUnderflowException e) {
                    log.error("{}: Processor {}, Incomplete message discarded from position {}: {}", nodeState.getNode(), (char)b, in.position(),
                            HexUtils.byteArrayToHexString(in.array()));
                }
            } else {
                log.error("{}: no processor is available for {} ({}), remaining content from position {} discarded: {}", nodeState.getNode(),
                        HexUtils.byteToHexString(b), (char)b, in.position(), HexUtils.byteArrayToHexString(in.array()));
                return;
            }
        }
    }

    void publish(Probe probe, Type type, Data data) {
        publisher.publish(probe, type, data);
    }
}
