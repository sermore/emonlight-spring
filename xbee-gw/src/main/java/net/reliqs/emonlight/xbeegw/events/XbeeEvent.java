package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.xbeegw.xbee.DataMessage;
import net.reliqs.emonlight.xbeegw.xbee.Processor;

import java.util.concurrent.TimeUnit;

class XbeeEvent extends DelayedEvent {

    private Processor processor;
    private DataMessage msg;

    XbeeEvent(Processor processor, DataMessage msg, long delay) {
        super(delay);
        this.processor = processor;
        this.msg = msg;
    }

    DataMessage getMsg() {
        return msg;
    }

    Processor getProcessor() {
        return processor;
    }

    void setProcessor(Processor processor) {
        this.processor = processor;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return processor == null ? Long.MAX_VALUE : super.getDelay(unit);
    }

    @Override
    public boolean isScheduled() {
        return false;
    }

    @Override
    public boolean process() {
        processor.processDataMessage(msg);
        return false;
    }

    @Override
    public String toString() {
        return "XE{msg=" + msg + ", " + super.toString() + '}';
    }
}
