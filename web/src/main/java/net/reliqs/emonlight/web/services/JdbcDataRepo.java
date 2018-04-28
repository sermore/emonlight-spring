package net.reliqs.emonlight.web.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Transactional(readOnly = true)
public class JdbcDataRepo implements DataRepo {
    private static final Logger log = LoggerFactory.getLogger(JdbcDataRepo.class);

    private static final String DATA_QUERY =
            "SELECT probe_id, value, time FROM data WHERE time > :tstart AND time <= :tend AND probe_id IN (:ids) ORDER BY probe_id, time LIMIT :limit";
    private static long limit = 10000;

    private NamedParameterJdbcOperations jdbcOperations;

    public JdbcDataRepo(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public Map<Long, List<Number[]>> getData(Iterable<Long> probeIds, long timeStart, long timeEnd) {
        Map<Long, List<Number[]>> result = new HashMap<>();
//        final Calendar cal = Calendar.getInstance();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("tstart", new Timestamp(timeStart));
        paramMap.put("tend", new Timestamp(timeEnd));
        paramMap.put("ids", probeIds);
        paramMap.put("limit", limit);
        final AtomicLong size = new AtomicLong();
        //        TimeZone.setDefault(TimeZone.getDefault().getTimeZone("GMT"));
        jdbcOperations.query(DATA_QUERY, paramMap, rch -> {
            Long pid = rch.getLong("probe_id");
            List<Number[]> list = result.get(pid);
            if (list == null) {
                list = new ArrayList<>();
                result.put(pid, list);
            }
            list.add(new Number[]{rch.getTimestamp("time").getTime(), rch.getDouble("value")});
            size.incrementAndGet();
        });
        log.trace("result size = {}", size);
        return result;
    }

}
