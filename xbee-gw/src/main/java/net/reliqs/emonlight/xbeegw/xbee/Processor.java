package net.reliqs.emonlight.xbeegw.xbee;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.HexUtils;

import net.reliqs.emonlight.xbeegw.config.Node;
import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;

@Component
public class Processor {
    private static final Logger log = LoggerFactory.getLogger(Processor.class);

    private final BlockingQueue<DataMessage> queue;
    private final Map<Byte, MessageProcessor> procs;
    private final XbeeGateway gateway;
    private final GlobalState globalState;
    private final Publisher publisher;
    @Value("${processor.timeout:1000}")
    private long timeout;

    @Autowired
    public Processor(final Settings settings, final XbeeGateway gateway, final GlobalState globalState, final Publisher publisher)
            throws XBeeException {
        this.gateway = gateway;
        this.globalState = globalState;
        this.publisher = publisher;
        queue = new LinkedBlockingQueue<>();
        procs = new HashMap<>();
        procs.put((byte) 'C', new ConfigurationProcessor(this));
        procs.put((byte) 'J', new DHT22Processor(this));
        procs.put((byte) 'V', new VCCProcessor(this));
        procs.put((byte) 'P', new PulseProcessor(this));
        procs.put((byte) 'D', new MultiDataProcessor(this));
        procs.put((byte) 'H', procs.get((byte) 'J'));
        procs.put((byte) 'W', procs.get((byte) 'V'));
        gateway.setProcessor(this);
        settings.getNodes().forEach(n -> register(gateway, n));
    }

    private void register(XbeeGateway gateway, Node n) {
        String addr = n.getAddress();
        NodeState ns = globalState.getNodeState(addr);
        ns.setDevice(gateway.addDevice(addr));
        for (Probe p : n.getProbes()) {
            if (p.hasThresholds()) {
                procs.get((byte) 'P').registerTrigger(ns, p);
            }
        }
    }

    void sendData(NodeState ns, byte[] data) {
        log.debug("{}: send {}", ns.getNode(), HexUtils.byteArrayToHexString(data));
        gateway.sendDataAsync(ns.getDevice(), data);
    }

    void queue(XBeeMessage msg) {
        queue.offer(new DataMessage(msg));
    }

    public void process() throws InterruptedException {
        do {
            //FIXME if a message is already received inside the timeout, the loop is never leaved and the dispatcher is never called
            DataMessage m = queue.poll(timeout, TimeUnit.MILLISECONDS);
            if (m != null) {
                processDataMessage(m);
            } else
                return;
        } while (true);
    }

    @PreDestroy
    public void cleanup() {
        gateway.cleanup();
    }

    private void processDataMessage(DataMessage m) {
        NodeState ns = globalState.getNodeState(m.msg.getDevice().get64BitAddress().toString());
        if (ns != null) {
            ByteBuffer in = ByteBuffer.wrap(m.msg.getData());
            processContent(m, ns, in);
        } else {
            log.warn("message {} discarded", m.msg);
        }
    }

    private void processContent(DataMessage m, NodeState nodeState, ByteBuffer in) {
        while (in.hasRemaining()) {
            byte b = in.get();
            MessageProcessor mp = procs.get(b);
            if (mp != null)
                try {
                    mp.process(m, nodeState, b, in);
                } catch (BufferUnderflowException e) {
                    log.error("{}: Incomplete message discarded {}", nodeState.getNode(),
                            HexUtils.byteArrayToHexString(in.array()));
                }
            else {
                log.error("{}: no processor is available for '{}', remaining content {} discarded", nodeState.getNode(),
                        (char) b, HexUtils.byteArrayToHexString(in.array()));
                return;
            }
        }
    }

    void publish(Probe probe, Type type, Data data) {
        publisher.publish(probe, type, data);
    }

}
