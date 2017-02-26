package net.reliqs.emonlight.xbeegw.send.services;

import java.util.Iterator;

import net.reliqs.emonlight.xbeegw.config.Settings;
import net.reliqs.emonlight.xbeegw.publish.Subscriber;
import net.reliqs.emonlight.xbeegw.xbee.Data;

public interface DeliveryService extends Subscriber {

	void post();
	boolean isReady();
	boolean isEmpty();
	
}
