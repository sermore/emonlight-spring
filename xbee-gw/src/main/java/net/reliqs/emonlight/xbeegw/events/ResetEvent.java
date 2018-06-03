package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.xbeegw.xbee.Processor;

public class ResetEvent extends DelayedEvent {

    private Processor processor;

    public ResetEvent(Processor processor) {
        super(0L);
        this.processor = processor;
    }

    @Override
    public boolean process() {
        processor.resetLocalDevice();
        return false;
    }

    @Override
    public String toString() {
        return "RE{" + super.toString() + '}';
    }
}
