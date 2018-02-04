package net.reliqs.emonlight.xbeegw.send.jpa;

import net.reliqs.emonlight.xbeegw.config.Probe;
import net.reliqs.emonlight.xbeegw.publish.Data;
import net.reliqs.emonlight.xbeegw.send.services.DeliveryService;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class JpaService implements DeliveryService, ListenableFutureCallback<Integer> {

    @Override
    public void post() {

    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void receive(Probe p, Probe.Type type, Data d) {

    }

    @Override
    public void onFailure(Throwable ex) {

    }

    @Override
    public void onSuccess(Integer result) {

    }
}
