package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;

/**
 * Created by sergio on 26/02/17.
 */
public interface TriggerHandler {

    /**
     * Trigger called when a threshold is trespassed. The parameters type and
     * enable define the new state of the alarm level that needs to be set.
     */
    void triggerChanged(Probe probe, Type type, int oldValue, int newValue);
}
