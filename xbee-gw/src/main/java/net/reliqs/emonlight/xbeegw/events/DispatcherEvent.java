package net.reliqs.emonlight.xbeegw.events;

import net.reliqs.emonlight.xbeegw.send.Dispatcher;

class DispatcherEvent extends DelayedEvent {

    private Dispatcher dispatcher;

    DispatcherEvent(Dispatcher dispatcher) {
        super(dispatcher.getRate());
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean isScheduled() {
        return true;
    }

    @Override
    public boolean process() {
        dispatcher.process();
        return false;
    }

    @Override
    public String toString() {
        return "DE{" + super.toString() + "}";
    }
}
