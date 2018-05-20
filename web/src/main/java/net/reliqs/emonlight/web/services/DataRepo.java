package net.reliqs.emonlight.web.services;

import java.util.List;
import java.util.Map;

public interface DataRepo {

    Map<Integer, List<Number[]>> getData(Iterable<Integer> probeIds, long timeStart, long timeEnd);

    Map<Integer, List<Number[]>> getResponseTime(Iterable<Integer> probeIds, long timeStart, long timeEnd);
}
