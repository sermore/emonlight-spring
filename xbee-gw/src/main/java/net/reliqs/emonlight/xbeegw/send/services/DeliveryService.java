package net.reliqs.emonlight.xbeegw.send.services;

import java.util.Iterator;

import net.reliqs.emonlight.xbeegw.xbee.Data;

public interface DeliveryService {
	
	void addInit(String server);
	void add(final int nodeId, final String apiKey, Iterator<Data> i);
	void addComplete();
	void post();
	boolean isReady();
	boolean isEmpty();
	
}
