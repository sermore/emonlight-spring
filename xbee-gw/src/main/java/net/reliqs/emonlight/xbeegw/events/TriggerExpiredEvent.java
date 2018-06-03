package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.xbeegw.monitoring.TriggerDataAbsent;

public class TriggerExpiredEvent extends DelayedEvent {

    private final TriggerDataAbsent triggerDataAbsent;
    private Node node;
    private int level;

    public TriggerExpiredEvent(TriggerDataAbsent triggerDataAbsent, Node node) {
        super(node.getTimeout());
        this.triggerDataAbsent = triggerDataAbsent;
        this.node = node;
        this.level = 0;
        reset();
    }

    public Node getNode() {
        return node;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    long getDelay() {
        return (level + 1) * node.getTimeout();
    }

    @Override
    public boolean isScheduled() {
        return level < triggerDataAbsent.getMaxLevel();
    }

    @Override
    public boolean process() {
        triggerDataAbsent.triggerFired(this);
        return false;
    }

    @Override
    public String toString() {
        return "TEE{N" + node.getId() + ", level=" + level + ", " + super.toString() + '}';
    }
}