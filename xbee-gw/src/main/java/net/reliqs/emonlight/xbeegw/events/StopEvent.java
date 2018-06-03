package net.reliqs.emonlight.xbeegw.events;

class StopEvent extends DelayedEvent {

    StopEvent(long delay) {
        super(delay);
    }

    @Override
    public boolean process() {
        return true;
    }

    @Override
    public String toString() {
        return "SE{" + super.toString() + '}';
    }
}
