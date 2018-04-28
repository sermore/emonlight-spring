package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.web.services.DataRepo;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Controller
public class ChartController {
    private static final Logger log = LoggerFactory.getLogger(ChartController.class);

    private DataRepo repo;
    @Autowired
    private ProbeMonitor monitor;


    @Autowired
    public ChartController(DataRepo repo) {
        super();
        this.repo = repo;
    }

    @RequestMapping("/live")
    public String live(@RequestParam(value = "id", required = true) Integer[] ids, Model model) {
        //		Node n = repo.findNode(id);
        //		if (n != null) {
        model.addAttribute("ids", ids);
        //			model.addAttribute("node", repo.findNode(id));
        //		}
        model.addAttribute("lastTime", monitor.get(11).getLastT());
        return "live";
    }

    @RequestMapping("/chartData")
    public @ResponseBody
    Map<Long, List<Number[]>> chartData(@RequestParam(value = "id[]", required = true) List<Long> ids,
            @RequestParam(value = "tstart", required = true, defaultValue = "0") long timeStart,
            @RequestParam(value = "tend", required = true, defaultValue = "-1") long timeEnd, Model model) {
        //        log.debug("ids = {}, timeStart = {}", Arrays.toString(ids), timeStart);
        //        Iterable<Number[]> d = repo.getData(Arrays.asList(ids), timeStart);
        if (timeEnd == -1) {
            timeEnd = Instant.now().toEpochMilli();
        }
        Map<Long, List<Number[]>> data = repo.getData(ids, timeStart, timeEnd);
        Number[] v = data.entrySet().stream().flatMap(e -> e.getValue().stream()).findFirst().orElse(new Number[]{0L, 0D});
        log.debug("ids = {}, timeStart = {}, timeEnd = {} -> size = {}, first {}, {}", ids, Instant.ofEpochMilli(timeStart),
                Instant.ofEpochMilli(timeEnd), data.size(), Instant.ofEpochMilli((long) v[0]), v[0]);
        return data;
    }

}
