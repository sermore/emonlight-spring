package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

/**
 * Created by sergio on 26/02/17.
 */
public interface TriggerHandler {
    void triggerChanged(NodeState nodeState, Probe probe, Type type, int oldValue, int newValue);
}
