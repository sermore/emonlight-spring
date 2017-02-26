package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.Data;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergio on 26/02/17.
 */
public class TriggerLevel {

    static public SimpleTrigger[] powerTriggers(final Probe p) {
        return new SimpleTrigger[]{
                new SimpleTrigger(null, p.getSoftThreshold()),
                new SimpleTrigger(new AverageCalc(p.getSoftThresholdTimeSec() * 920 / 10800), p.getSoftThreshold()),
                new SimpleTrigger(new AverageCalc(p.getSoftThresholdTimeSec() * 1840 / 10800), p.getSoftThreshold()),
                new SimpleTrigger(null, p.getHardThreshold())
        };
    }

    private final Probe probe;
    private final NodeState nodeState;
    private final SimpleTrigger[] triggers;
    private int triggerState;
    private List<TriggerHandler> handlers;

    public TriggerLevel(final Probe p, final NodeState ns, final SimpleTrigger[] triggers) {
        probe = p;
        nodeState = ns;
        this.triggers = triggers;
        this.handlers = new ArrayList<>();
    }

    public void addHandler(TriggerHandler h) {
        handlers.add(h);
    }

    public int process(Data d) {
        boolean[] triggerValues = new boolean[triggers.length];
        int maxLevel = 0;
        for (int i = 0; i < triggers.length; i++) {
            triggerValues[i] = triggers[i].process(d);
            if (triggerValues[i]) {
                maxLevel = i;
            }
        }
        int newTriggerState;
        if (!triggerValues[0]) {
            // if lower trigger is not active and we came from an active state, then the trigger goes off, no matter what was its state before
            newTriggerState = 0;
        } else {
            // lower trigger is active, identify the higher active level
            newTriggerState = maxLevel + 1;
        }
        if (newTriggerState != triggerState) {
            triggerChanged(triggerState, newTriggerState);
        }
        triggerState = newTriggerState;
        return triggerState;
    }

    private void triggerChanged(int oldTriggerState, int newTriggerState) {
        for (TriggerHandler h: handlers) {
            h.triggerChanged(nodeState, probe, oldTriggerState, newTriggerState);
        }
    }

//    boolean lowerTriggerIsActive = triggerState.contains(Probe.Type.M_SOFT_THRESHOLD_1);
//    Probe.Type newTriggerState = triggerState.peek();
//		if (!lowerTriggerIsActive) {
//        // if lower trigger is not active and we came from an active state,
//        // then we trigger goes off, no matter what
//        // trigger was active before
//        if (previousTriggerState != null) {
//            processor.trigger(nodeState, probe, previousTriggerState, false);
//            previousTriggerState = null;
//        }
//    } else if (newTriggerState != previousTriggerState) {
//        // if the higher trigger is changed
//        if (newTriggerState != null) {
//            processor.trigger(nodeState, probe, newTriggerState, true);
//        } else {
//            processor.trigger(nodeState, probe, previousTriggerState, false);
//            assert false;
//        }
//        previousTriggerState = newTriggerState;
//    }


}
