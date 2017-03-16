package net.reliqs.emonlight.web.entities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by sergio on 19/02/17.
 */
public interface SampleRepo extends JpaRepository<Sample, Long> {

    List<Sample> getSamplesByNode_IdAndSampleTimeGreaterThan(Long nodeId, Timestamp from);

    //    @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MIN_VALUE))
    Stream<Sample> readSamplesByNode_IdAndSampleTimeGreaterThan(Long nodeId, Timestamp from);
}
