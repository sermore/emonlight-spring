package net.reliqs.emonlight.xbeegw.xbee.data;

import java.util.Iterator;

import net.reliqs.emonlight.xbeegw.xbee.Data;

interface ProbeDataOut {

	Iterator<Data> getIterator();

	void clear();
	
	boolean isEmpty();

	int queueLength();
	
	Data peek();

	Data poll();
	
}
