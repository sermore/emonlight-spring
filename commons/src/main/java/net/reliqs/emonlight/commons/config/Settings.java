package net.reliqs.emonlight.commons.config;

import net.reliqs.emonlight.commons.config.Node.OpMode;
import net.reliqs.emonlight.commons.config.annotations.ValidSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import javax.validation.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Validated
@ValidSettings
public class Settings {
    private static final Logger log = LoggerFactory.getLogger(Settings.class);

    @Size(min = 4)
    private String serialPort;

    @Min(9600)
    @Max(115200)
    private int baudRate = 115200;

    @Min(500)
    @Max(28000)
    private int receiveTimeout = 2000;

    @Size(min = 1)
    @Valid
    private List<Node> nodes = new ArrayList<>();

    @Valid
    private List<Server> servers = new ArrayList<>();

    @NotNull
    @Min(0)
    private Integer idCnt;

    private Map<String, Node> nodeMap;

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    public int getReceiveTimeout() {
        return receiveTimeout;
    }

    public void setReceiveTimeout(int receiveTimeout) {
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

    static Settings load(String pathName) {
        Path path = Paths.get(pathName);
        Yaml yaml = new Yaml();
        try (
                Reader reader = Files.newBufferedReader(path)
        ) {
            Settings s = (Settings) yaml.load(reader);
//            Map<String, Settings> map = new HashMap<>();
//            map.put("settings", this);
            log.debug("loaded Settings from {}", path.toAbsolutePath());
            return s;
        } catch (IOException e) {
            log.error("Error reading settings file from " + path.toAbsolutePath(), e);
        }
        return null;
    }

    static void validate(Settings s) {
        if (s == null) {
            throw new ValidationException("Settings null");
        }
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        Validator validator = vf.getValidator();
        Set<ConstraintViolation<Settings>> res = validator.validate(s);
        if (!res.isEmpty()) {
            String msg = res.stream().map(c -> String.format("%s -> %s", c.getPropertyPath(), c.getMessage())).collect(Collectors.joining(", "));
            throw new ValidationException("Settings invalid: " + msg);
        }
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
    void init() {
        // connect probes to references inside ServerMap items
        getServers().stream().flatMap(srv -> srv.getMaps().stream()).forEach(sm -> {
            Probe p = getNodes().stream().flatMap(nn -> nn.getProbes().stream())
                    .filter(pp -> pp.getName().equals(sm.getProbe().getName())).findFirst().get();
            sm.setProbe(p);
        });
        nodeMap = new HashMap<>(nodes.size());
        // fill probe's fields
        getNodes().forEach(n -> {
            nodeMap.put(n.getName(), n);
            n.getProbes().forEach(p -> {
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

    boolean dump(String pathName) {
        Path path = Paths.get(pathName);
        Yaml yaml = new Yaml();
        try (
                Writer writer = Files.newBufferedWriter(path)
        ) {
//            Map<String, Settings> map = new HashMap<>();
//            map.put("settings", this);
            yaml.dump(this, writer);
            return true;
        } catch (IOException e) {
            log.error("Error saving settings file to " + path.toAbsolutePath(), e);
        }
        return false;
    }

}
