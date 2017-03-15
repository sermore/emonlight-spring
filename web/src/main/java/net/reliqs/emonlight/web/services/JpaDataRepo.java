package net.reliqs.emonlight.web.services;

import net.reliqs.emonlight.web.entities.Node;
import net.reliqs.emonlight.web.entities.NodeRepo;
import net.reliqs.emonlight.web.entities.Sample;
import net.reliqs.emonlight.web.entities.SampleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergio on 19/02/17.
 */
@Transactional
@Service
@Repository
public class JpaDataRepo implements DataRepo {
    private static final Logger log = LoggerFactory.getLogger(JpaDataRepo.class);

    private NodeRepo nodeRepo;

    private SampleRepo sampleRepo;

    private int i;

    @Autowired
    public JpaDataRepo(NodeRepo nodeRepo, SampleRepo sampleRepo) {
        this.nodeRepo = nodeRepo;
        this.sampleRepo = sampleRepo;
    }

    @Override
    public Node findNode(long id) {
        return nodeRepo.findOne(id);
    }

    @Override
    public Node findNodeByAuthenticationToken(String token) {
        return nodeRepo.findByAuthenticationToken(token);
    }

    @Override
    public Iterable<Number[]> getData(Iterable<Long> nodeIds, long timeStart) {
        List<Number[]> data = new ArrayList<Number[]>();
        Timestamp tstart = new Timestamp(timeStart);

        i = 0;
        for (Long id : nodeIds) {
            sampleRepo.readSamplesByNode_IdAndSampleTimeGreaterThan(id, tstart).forEach(s -> data.add(new Number[]{i, s.getSampleTime().getTime(), s.getValue()}));
            i++;
        }
        log.debug("DATA {}", data);
        return data;
    }

    @Override
    public List<Sample> getSamples(Long nodeId, Timestamp from) {
        return sampleRepo.getSamplesByNode_IdAndSampleTimeGreaterThan(nodeId, from);
    }

    @Override
    public Node saveNode(Node node) {
        return nodeRepo.save(node);
    }

    @Override
    public List<Sample> saveSamples(Iterable<Sample> samples) {
        return sampleRepo.save(samples);
    }
}
