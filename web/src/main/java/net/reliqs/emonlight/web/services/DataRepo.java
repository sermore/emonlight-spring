package net.reliqs.emonlight.web.services;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public interface DataRepo {

    Map<Integer, List<Number[]>> getData(Iterable<Integer> probeIds, long timeStart, long timeEnd);

    void forEach(Iterable<Integer> probeIds, long timeStart, long timeEnd, BiConsumer<Integer, Number[]> func);

    Map<Integer, List<Number[]>> getResponseTime(Iterable<Integer> probeIds, long timeStart, long timeEnd);
}
