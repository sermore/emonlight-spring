package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.web.services.DataRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Controller
public class ChartController {
    private static final Logger log = LoggerFactory.getLogger(ChartController.class);

    private DataRepo repo;

    @Autowired
    public ChartController(DataRepo repo) {
        super();
        this.repo = repo;
    }

    @RequestMapping("/live")
    public String live(@RequestParam(value = "id[]", required = true) Long[] ids, Model model) {
//		Node n = repo.findNode(id);
//		if (n != null) {
        model.addAttribute("ids", ids);
//			model.addAttribute("node", repo.findNode(id));
//		}
        return "live";
    }

    @RequestMapping("/data")
    public
    @ResponseBody
    Map<Long, List<Number[]>> data(@RequestParam(value = "id[]", required = true) List<Long> ids,
                                   @RequestParam(value = "tstart", required = true, defaultValue = "0") long timeStart,
                                   @RequestParam(value = "tend", required = true, defaultValue = "-1") long timeEnd,
                                   @RequestParam(value = "tzone", required = true, defaultValue = "0") int tzone,
                                   Model model) {
//        log.debug("ids = {}, timeStart = {}", Arrays.toString(ids), timeStart);
//        Iterable<Number[]> d = repo.getData(Arrays.asList(ids), timeStart);
        if (timeEnd == -1) {
            timeEnd = Instant.now().atOffset(ZoneOffset.ofTotalSeconds(tzone * 60)).toInstant().toEpochMilli();
        }
        Map<Long, List<Number[]>> data = repo.getData(ids, timeStart, timeEnd, tzone);
        log.debug("ids = {}, timeStart = {}, timeEnd = {}, tzone = {} -> size = {}", ids, timeStart, timeEnd, tzone, data.size());
        return data;
    }

}
