package net.reliqs.emonlight.xbeegw.monitoring;

import java.util.ArrayList;
import java.util.List;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.publish.Subscriber;

abstract class Trigger implements Subscriber {

    private List<TriggerHandler> handlers;

    protected Trigger() {
        this.handlers = new ArrayList<>();
    }

    void addHandler(TriggerHandler h) {
        handlers.add(h);
    }

    @Override
    public void receive(Probe probe, Type type, Data data) {
        // avoid loop in publish / subscribe
        if (isApplicable(probe, type, data)) {
            // log.trace("{}: process trigger {}", probe.getNode(), type);
            process(probe, data);
        }
    }

    protected void triggerChanged(Probe probe, Type type, int oldTriggerState, int newTriggerState) {
        for (TriggerHandler h: handlers) {
            h.triggerChanged(probe, type, oldTriggerState, newTriggerState);
        }
    }

    boolean isApplicable(Probe probe, Type type, Data data) {
        return type == probe.getType();
    }
    
    abstract void process(Probe probe, Data data);

}
