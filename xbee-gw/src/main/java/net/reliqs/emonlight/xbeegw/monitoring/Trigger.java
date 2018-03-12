package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Subscriber;

import java.util.ArrayList;
import java.util.List;

abstract class Trigger implements Subscriber {

    private List<TriggerHandler> handlers;

    protected Trigger() {
        this.handlers = new ArrayList<>();
    }

    void addHandler(TriggerHandler h) {
        handlers.add(h);
    }

    @Override
    public void receive(Probe probe, Probe.Type type, Data data) {
        // avoid loop in publish / subscribe
        if (isApplicable(probe, type, data)) {
            // log.trace("{}: process trigger {}", probe.getNode(), type);
            process(probe, data);
        }
    }

    void triggerChanged(Probe probe, Probe.Type type, int oldTriggerState, int newTriggerState) {
        for (TriggerHandler h : handlers) {
            h.triggerChanged(probe, type, oldTriggerState, newTriggerState);
        }
    }

    boolean isApplicable(Probe probe, Probe.Type type, Data data) {
        return type == probe.getType();
    }

    abstract void process(Probe probe, Data data);

}
