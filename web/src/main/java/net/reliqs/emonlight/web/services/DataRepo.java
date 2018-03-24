package net.reliqs.emonlight.web.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface DataRepo {

    @Transactional(readOnly = true)
    Map<Long, List<Number[]>> getData(Iterable<Long> probeIds, long timeStart, long timeEnd, int tzone);

}
