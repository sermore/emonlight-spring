package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.xbeegw.xbee.Processor;

public class ThresholdAlarmEvent extends DelayedEvent {

    private Processor processor;
    private Probe probe;
    private int level;

    public ThresholdAlarmEvent(Processor processor, Probe probe, int level) {
        super(0L);
        this.processor = processor;
        this.probe = probe;
        this.level = level;
    }

    @Override
    public boolean process() {
        processor.sendBuzzerCmd(probe, level);
        return false;
    }

    @Override
    public String toString() {
        return "TAE{P" + probe.getId() + ", level=" + level + ", " + super.toString() + '}';
    }
}
