package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.Sample;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public interface DataRepo {

	Node findNode(long id);

	Node findNodeByAuthenticationToken(String token);

	@Transactional(readOnly = true)
	Iterable<Number[]> getData(Iterable<Long> nodeIds, long timeStart);

	@Transactional(readOnly = true)
	List<Sample> getSamples(Long nodeId, Timestamp from);

	Node saveNode(Node node);

	List<Sample> saveSamples(Iterable<Sample> samples);

}
