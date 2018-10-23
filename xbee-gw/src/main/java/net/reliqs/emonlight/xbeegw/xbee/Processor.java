package net.reliqs.emonlight.xbeegw.xbee;

import com.digi.xbee.api.utils.HexUtils;
import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Probe.Type;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.data.Data;
import net.reliqs.emonlight.xbeegw.events.EventProcessorFacade;
import net.reliqs.emonlight.xbeegw.publish.Publisher;
import net.reliqs.emonlight.xbeegw.state.GlobalState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Processor of the xbee events.
 * <p>
 * Handle a queue containing the messages received from remote xbee devices. The
 * processing of every message could produce a response message to be sent to
 * the xbee, or/and produce a data output to be published. The details of the
 * message processing is handled by specific MessageProcessor instances,
 * selected using the first byte of the received message.
 */
@Service
public class Processor {
    private static final Logger log = LoggerFactory.getLogger(Processor.class);

    private final Map<Byte, MessageProcessor> procs;
    private final XbeeProcessor gateway;
    private final GlobalState globalState;
    private final Publisher publisher;

    public Processor(final Settings settings, final XbeeProcessor gateway, final GlobalState globalState, final Publisher publisher,
            EventProcessorFacade eventProcessorFacade) {
        log.info("processor configuration");
        this.gateway = gateway;
        this.globalState = globalState;
        this.publisher = publisher;
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
        settings.getNodes().forEach(n -> registerNode(n));
        log.debug("processor configuration complete");
        eventProcessorFacade.setProcessor(this);
    }

    private void registerNode(Node n) {
        log.info("register node {}", n);
        String addr = n.getAddress();
        NodeState ns = globalState.getNodeState(addr);
        ns.setDevice(gateway.addDevice(addr));
    }

    void sendData(NodeState ns, byte[] data) {
        log.debug("{}: send {}", ns.getNode(), HexUtils.byteArrayToHexString(data));
        gateway.sendDataAsync(ns.getDevice(), data);
    }

    void sendBuzzerAlarmLevel(NodeState ns, int level) {
        ByteBuffer b = ByteBuffer.allocate(3);
        b.put((byte) 'S');
        b.put((byte) 'B');
        b.put((byte) level);
        sendData(ns, b.array());
    }

    public void resetLocalDevice() {
        log.warn("try to reset local Xbee device");
        gateway.resetLocalDevice();
        log.info("reset local Xbee device completed");
    }

    public void sendBuzzerCmd(Probe probe, int level) {
        log.info("{}: {} Buzzer level {}", probe.getNode(), probe.getName(), level);
        sendBuzzerAlarmLevel(globalState.getNodeState(probe.getNode().getAddress()), level);
    }

    public void sendRemoteReset(Node node) {
        log.warn("try to reset remote Xbee device {}", node);
        gateway.resetRemoteDevice(globalState.getNodeState(node.getAddress()).getDevice());
        log.info("reset remote Xbee device {} completed", node);
    }

    public void processDataMessage(DataMessage m) {
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
        assert probe != null && type != null && data != null;
        publisher.publish(probe, type, data);
    }
}
