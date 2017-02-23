package net.reliqs.emonlight.web.entities;

import org.hibernate.Interceptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

/**
 * Created by sergio on 19/02/17.
 */
public interface SampleRepo extends JpaRepository<Sample, Long> {

    List<Sample> getSamplesByNode_IdAndSampleTimeGreaterThan(Long nodeId, Timestamp from);

//    @QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MIN_VALUE))
    Stream<Sample> readSamplesByNode_IdAndSampleTimeGreaterThan(Long nodeId, Timestamp from);
}
