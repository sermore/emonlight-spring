package net.reliqs.emonlight.web.services;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface DataRepo {

//    Node findNode(long id);
//
//    Node findNodeByAuthenticationToken(String token);

    @Transactional(readOnly = true)
    Map<Long, List<Number[]>> getData(Iterable<Long> probeIds, long timeStart, long timeEnd, int tzone);

//    @Transactional(readOnly = true)
//    List<Sample> getSamples(Long nodeId, Timestamp from);
//
//    Node saveNode(Node node);
//
//    List<Sample> saveSamples(Iterable<Sample> samples);

}
