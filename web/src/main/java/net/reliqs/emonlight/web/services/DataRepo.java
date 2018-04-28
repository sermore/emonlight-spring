package net.reliqs.emonlight.web.services;

import java.util.List;
import java.util.Map;

public interface DataRepo {

    Map<Long, List<Number[]>> getData(Iterable<Long> probeIds, long timeStart, long timeEnd);

}
