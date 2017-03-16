package net.reliqs.emonlight.xbeegw.monitoring;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.config.Probe.Type;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.xbee.NodeState;

/**
 * Created by sergio on 26/02/17.
 */
public class TriggerLevel extends Trigger {

    private final Probe probe;
    //    private final NodeState nodeState;
    private final SimpleTrigger[] triggers;
    private int triggerState;

    public TriggerLevel(final Probe p, final NodeState ns, final SimpleTrigger[] triggers) {
        probe = p;
//        nodeState = ns;
        this.triggers = triggers;
//        this.handlers = new ArrayList<>();
    }
//    private List<TriggerHandler> handlers;

    static public SimpleTrigger[] powerTriggers(final Probe p) {
        return new SimpleTrigger[]{
                new SimpleTrigger(null, p.getSoftThreshold()),
                new SimpleTrigger(new AverageCalc(p.getSoftThresholdTimeSec() * 920 / 10800), p.getSoftThreshold()),
                new SimpleTrigger(new AverageCalc(p.getSoftThresholdTimeSec() * 1840 / 10800), p.getSoftThreshold()),
                new SimpleTrigger(null, p.getHardThreshold())
        };
    }

//    public void addHandler(TriggerHandler h) {
//        handlers.add(h);
//    }

    @Override
    boolean isApplicable(Probe probe, Type type, Data data) {
        return super.isApplicable(probe, type, data) && probe == this.probe;
    }

    @Override
    void process(Probe pulse, Data d) {
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
            triggerChanged(probe, Type.THRESOLD_ALARM, triggerState, newTriggerState);
        }
        triggerState = newTriggerState;
    }

//    private void triggerChanged(int oldTriggerState, int newTriggerState) {
//        for (TriggerHandler h: handlers) {
//            h.triggerChanged(nodeState, probe, Probe.Type.THRESOLD_ALARM, oldTriggerState, newTriggerState);
//        }
//    }

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
