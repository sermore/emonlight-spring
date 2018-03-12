package net.reliqs.emonlight.xbeegw.state;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalState {

    private final Map<String, NodeState> nodes;

    @Autowired
    public GlobalState(Settings settings) {
        super();
        this.nodes = new HashMap<>();
        // create node's state for each node
        settings.getNodes().forEach(n -> {
            NodeState ns = new NodeState(this, n);
            nodes.put(n.getAddress(), ns);
        });
    }

    public NodeState getNodeState(String address) {
        return nodes.get(address);
    }

}
