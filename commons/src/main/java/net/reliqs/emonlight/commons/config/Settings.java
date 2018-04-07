package net.reliqs.emonlight.commons.config;

import net.reliqs.emonlight.commons.config.Node.OpMode;
import net.reliqs.emonlight.commons.config.annotations.ValidSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

//@Validated
@ValidSettings
public class Settings implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(Settings.class);

    static final long serialVersionUID = 1L;

    @NotNull
    @Size(min = 4)
    private String serialPort;

    @NotNull
    @Min(9600)
    @Max(115200)
    private Integer baudRate = 115200;

    @NotNull
    @Min(500)
    @Max(28000)
    private Integer receiveTimeout = 2000;

    @Size(min = 1)
    @Valid
    private List<Node> nodes = new ArrayList<>();

    @Valid
    private List<Server> servers = new ArrayList<>();

    @NotNull
    @Min(0)
    private Integer idCnt;

    private Map<String, Node> nodeMap;
    private Map<Integer, Node> nodeMapId;
    private Map<Integer, Probe> probeMapId;

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public Integer getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(Integer baudRate) {
        this.baudRate = baudRate;
    }

    public Integer getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(Integer receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public Integer getIdCnt() {
        return idCnt;
    }

    public int findMaxSampleTime() {
        int sampleTime = getNodes().stream()
                .mapToInt(n -> Math.max(n.getSampleTime(),
                        n.getMode() == OpMode.DHT22 ? 0 : n.getProbes().stream().mapToInt(p -> p.getSampleTime()).filter(s -> s > 0).min().orElse(0)))
                .max().getAsInt();
        return sampleTime;
    }

    public Stream<Probe> getProbes() {
        return getNodes().stream().flatMap(n -> n.getProbes().stream());
    }

    public void setIdCnt(Integer idCnt) {
        this.idCnt = idCnt;
    }

    @PostConstruct
    public void init() {
        // connect probes to references inside ServerMap items
        getServers().stream().flatMap(srv -> srv.getMaps().stream()).forEach(sm -> {
            Probe p = getNodes().stream().flatMap(nn -> nn.getProbes().stream())
                    .filter(pp -> pp.getName().equals(sm.getProbe().getName())).findFirst().get();
            sm.setProbe(p);
        });
        nodeMap = new HashMap<>(nodes.size());
        nodeMapId = new HashMap<>(nodes.size());
        probeMapId = new HashMap<>();
        // fill probe's fields
        getNodes().forEach(n -> {
            nodeMap.put(n.getName(), n);
            nodeMapId.put(n.getId(), n);
            n.getProbes().forEach(p -> {
                probeMapId.put(p.getId(), p);
                // init default value for port
                if (p.getPort() == 0) {
                    p.setPort(n.getDefaultPort(p.getType()));
                }
                p.setNode(n);
            });
            n.initMaps();
        });
        // fix ids
        AtomicInteger idCnt = new AtomicInteger(Math.max(getNodes().stream().mapToInt(Node::getId).max().orElse(1), getProbes().mapToInt(Probe::getId).max().orElse(1)));
        getNodes().stream().filter(n -> n.getId() == null).forEach(n -> n.setId(idCnt.getAndIncrement()));
        getProbes().filter(p -> p.getId() == null).forEach(p -> p.setId(idCnt.getAndIncrement()));

        log.debug("initialized nodes={}, servers={}", getNodes().size(), getServers().size());
    }

    public Node findNodeByName(String name) {
        return nodeMap.get(name);
    }

    public Node findNodeById(Integer node) {
        return nodeMapId.get(node);
    }

    public Probe findProbeById(Integer probe) {
        return probeMapId.get(probe);
    }

    public Node addNewNode() {
        Node node = new Node();
        node.setId(idCnt++);
        node.setName("Node " + node.getId());
        nodes.add(node);
        return node;
    }

    public Node removeNode(int nodeIndex) {
        Node node = nodes.remove(nodeIndex);
        if (node != null) {
            log.debug("removed {}", node);
            if (nodeMap != null) {
                nodeMap.remove(node.getName());
            }
            if (nodeMapId != null) {
                nodeMapId.remove(node.getId());
            }
            if (probeMapId != null) {
                for (Probe probe : node.getProbes()) {
                    probeMapId.remove(probe.getId());
                }
            }
        } else {
            log.warn("fail to remove {} {}", nodeIndex, node);
        }
        return node;
    }

    public Probe addNewProbe(Integer nodeIndex) {
        Probe p = null;
        Node n = nodes.get(nodeIndex);
        if (n != null) {
            p = new Probe();
            p.setId(idCnt++);
            p.setName("Probe " + p.getId());
            p.setNode(n);
            n.getProbes().add(p);
            probeMapId.put(p.getId(), p);
        }
        return p;
    }

    public Probe removeProbe(Integer nodeIndex, Integer probeIndex) {
        if (nodeIndex != null && probeIndex != null) {
            Node node = nodes.get(nodeIndex);
            if (node != null) {
                Probe probe = node.getProbes().remove(probeIndex.intValue());
                if (probe != null) {
                    probeMapId.remove(probe.getId());
                    return probe;
                }
            }
        }
        return null;
    }

    public Server addNewServer() {
        Server s = new Server();
        s.setName("Server " + getServers().size() + 1);
        servers.add(s);
        return s;
    }

    public ServerMap addNewServerMap(Integer serverIndex) {
        ServerMap sm = null;
        Server s = servers.get(serverIndex);
        if (s != null) {
            sm = new ServerMap();
            s.getMaps().add(sm);
        }
        return sm;
    }

    public Server removeServer(Integer serverIndex) {
        Server s = servers.remove(serverIndex.intValue());
        return s;
    }

    public ServerMap removeServerMap(Integer serverIndex, Integer serverMapIndex) {
        ServerMap sm = null;
        Server s = servers.get(serverIndex);
        if (s != null) {
            sm = s.getMaps().remove(serverMapIndex.intValue());
        }
        return sm;
    }
}
