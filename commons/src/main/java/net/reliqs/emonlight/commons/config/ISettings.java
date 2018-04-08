package net.reliqs.emonlight.commons.config;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

public interface ISettings extends Serializable {
    String getSerialPort();

    void setSerialPort(String serialPort);

    Integer getBaudRate();

    void setBaudRate(Integer baudRate);

    Integer getReceiveTimeout();

    void setReceiveTimeout(Integer receiveTimeout);

    List<Node> getNodes();

    void setNodes(List<Node> nodes);

    List<Server> getServers();

    void setServers(List<Server> servers);

    Integer getIdCnt();

    void setIdCnt(Integer idCnt);

    int findMaxSampleTime();

    Stream<Probe> getProbes();

    @PostConstruct
    void init();

    Node findNodeByName(String name);

    Node findNodeById(Integer node);

    Probe findProbeById(Integer probe);

    Node addNewNode();

    Node removeNode(int nodeIndex);

    Probe addNewProbe(Integer nodeIndex);

    Probe removeProbe(Integer nodeIndex, Integer probeIndex);

    Server addNewServer();

    ServerMap addNewServerMap(Integer serverIndex);

    Server removeServer(Integer serverIndex);

    ServerMap removeServerMap(Integer serverIndex, Integer serverMapIndex);
}
