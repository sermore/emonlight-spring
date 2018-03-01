package net.reliqs.emonlight.xbeegw.send.services;

import net.reliqs.emonlight.xbeegw.publish.Subscriber;

public interface DeliveryService extends Subscriber {

    void post();

    boolean isReady();

    boolean isQueueEmpty();

}
