package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.xbeegw.xbee.Processor;

public class MissingAlarmEvent extends DelayedEvent {

    private Processor processor;
    private Node node;

    public MissingAlarmEvent(Processor processor, Node node) {
        super(0L);
        this.processor = processor;
        this.node = node;
    }

    @Override
    public boolean process() {
        //        processor.sendRemoteReset(node);
        return false;
    }

    @Override
    public String toString() {
        return "MAE{N" + node.getId() + ", " + super.toString() + "}";
    }
}
