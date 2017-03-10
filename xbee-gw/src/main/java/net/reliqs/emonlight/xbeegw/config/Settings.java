package net.reliqs.emonlight.xbeegw.config;

import net.reliqs.emonlight.xbeegw.config.Node.OpMode;
import net.reliqs.emonlight.xbeegw.config.annotations.ValidNodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@ConfigurationProperties(prefix = "settings")
@Validated
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
    @ValidNodes
    private List<Node> nodes;

    @Valid
    private List<Server> servers = new ArrayList<>();


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

    @PostConstruct
    void init() {
        // connect probes to references inside ServerMap items
        getServers().stream().flatMap(srv -> srv.getMaps().stream()).forEach(sm -> {
            Probe p = getNodes().stream().flatMap(nn -> nn.getProbes().stream())
                    .filter(pp -> pp.getName().equals(sm.getProbe().getName())).findFirst().get();
            sm.setProbe(p);
        });
        // fill probe's fields
        getNodes().forEach(n -> {
            n.getProbes().forEach(p -> {
                // init default value for port
                if (p.getPort() == 0) {
                    p.setPort(n.getDefaultPort(p.getType()));
                }
                p.setNode(n);
            });
            n.initProbeMap();
        });
        log.debug("Settings nodes={}, servers={}", getNodes().size(), getServers().size());
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
}
