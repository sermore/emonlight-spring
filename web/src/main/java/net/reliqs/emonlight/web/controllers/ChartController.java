package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.web.services.DataRepo;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import net.reliqs.emonlight.web.stats.StatsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @RequestMapping("/probe")
    public String probe(@RequestParam(value = "id", required = true) Integer id, Model model, HttpSession session) {
        Settings settings = (Settings) session.getAttribute("settings");
        Optional<Probe> p = settings.getProbes().filter(pp -> pp.getId().equals(id)).findFirst();
        if (p.isPresent()) {
            model.addAttribute("id", id);
            model.addAttribute("probe", p.get());
            model.addAttribute("timezone", settings.getTzone());
            model.addAttribute("yTitle", yTitle(p.get()));
        }
        return "probe";
    }

    @RequestMapping("/node")
    public String node(@RequestParam(value = "id", required = true) Integer id, Model model, HttpSession session) {
        Settings settings = (Settings) session.getAttribute("settings");
        Node n = settings.findNodeById(id);
        if (n != null) {
            model.addAttribute("id", id);
            model.addAttribute("node", n);
            model.addAttribute("nodeData", nodeData(n));
            model.addAttribute("timezone", settings.getTzone());
            //            model.addAttribute("yTitle", yTitle(p.get()));
        }
        return "node";
    }

    @RequestMapping("/nodes_dashboard")
    public String nodesDashboard(Model model, HttpSession session) {
        Settings settings = (Settings) session.getAttribute("settings");
        model.addAttribute("timeData", nodesTimeData(settings));
        model.addAttribute("vccData", nodesVccData(settings));
        model.addAttribute("timezone", settings.getTzone());
        return "nodes_dashboard";
    }

    @RequestMapping("/data_dashboard")
    public String dataDashboard(Model model, HttpSession session) {
        Settings settings = (Settings) session.getAttribute("settings");
        model.addAttribute("powerData", powerData(settings));
        model.addAttribute("tempData", tempData(settings));
        model.addAttribute("humData", humData(settings));
        model.addAttribute("timezone", settings.getTzone());
        return "data_dashboard";
    }

    private JsonObject nodesTimeData(Settings settings) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        JsonArrayBuilder series = Json.createArrayBuilder();
        JsonArrayBuilder ids = Json.createArrayBuilder();
        for (Node n : settings.getNodes()) {
            Probe main = n.findProbeByType(mainProbe(n));
            ids.add(main.getId());
            series.add(Json.createObjectBuilder().add("name", main.getName()).add("id", main.getId().toString()));
        }
        return b.add("ids", ids).add("series", series).build();
    }

    private JsonObject nodesVccData(Settings settings) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        JsonArrayBuilder series = Json.createArrayBuilder();
        JsonArrayBuilder ids = Json.createArrayBuilder();
        settings.getNodes().stream().filter(Node::isEndDevice).forEach(n -> {
            Probe vcc = n.findProbeByType(Probe.Type.VCC);
            ids.add(vcc.getId());
            series.add(Json.createObjectBuilder().add("name", vcc.getName()).add("id", vcc.getId().toString()));
        });

        return b.add("ids", ids).add("series", series).build();
    }

    private JsonObject powerData(Settings settings) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        JsonArrayBuilder series = Json.createArrayBuilder();
        JsonArrayBuilder ids = Json.createArrayBuilder();
        settings.getProbes().filter(p -> p.getType() == Probe.Type.PULSE).forEach(p -> {
            ids.add(p.getId());
            series.add(Json.createObjectBuilder().add("name", p.getName()).add("id", p.getId().toString()));
        });

        return b.add("ids", ids).add("series", series).build();
    }

    private JsonObject tempData(Settings settings) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        JsonArrayBuilder series = Json.createArrayBuilder();
        JsonArrayBuilder ids = Json.createArrayBuilder();
        settings.getProbes().filter(p -> p.getType() == Probe.Type.DHT22_T || p.getType() == Probe.Type.DS18B20).forEach(p -> {
            ids.add(p.getId());
            series.add(Json.createObjectBuilder().add("name", p.getName()).add("id", p.getId().toString()));
        });

        return b.add("ids", ids).add("series", series).build();
    }

    private JsonObject humData(Settings settings) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        JsonArrayBuilder series = Json.createArrayBuilder();
        JsonArrayBuilder ids = Json.createArrayBuilder();
        settings.getProbes().filter(p -> p.getType() == Probe.Type.DHT22_H).forEach(p -> {
            ids.add(p.getId());
            series.add(Json.createObjectBuilder().add("name", p.getName()).add("id", p.getId().toString()));
        });

        return b.add("ids", ids).add("series", series).build();
    }

    private JsonObject nodeData(Node n) {
        JsonObjectBuilder b = Json.createObjectBuilder();
        JsonArrayBuilder list = Json.createArrayBuilder();
        Probe main = n.findProbeByType(mainProbe(n));
        Probe vcc = n.findProbeByType(Probe.Type.VCC);
        return b.add("name", n.getName()).add("ids", Json.createArrayBuilder().add(main.getId()).add(vcc.getId())).add("probes",
                list.add(Json.createObjectBuilder().add("id", main.getId().toString()).add("name", main.getName()))
                        .add(Json.createObjectBuilder().add("id", vcc.getId().toString()).add("name", vcc.getName()))).build();
    }

    private Probe.Type mainProbe(Node n) {
        Probe.Type t = null;
        switch (n.getMode()) {
            case PULSE_DHT22:
            case PULSE_DS18B20:
            case PULSE:
                t = Probe.Type.PULSE;
                break;
            case DHT22:
                t = Probe.Type.DHT22_T;
                break;
            case DS18B20:
                t = Probe.Type.DS18B20;
                break;
            default:
                Assert.state(false, "node mode not supported");
        }
        return t;
    }


    private String yTitle(Probe p) {
        switch (p.getType()) {
            case PULSE:
                return "Power (W)";
            case DHT22_H:
                return "Humidity (%)";
            case DS18B20:
            case DHT22_T:
                return "Temperature (°C)";
            case VCC:
                return "Voltage (V)";
            default:
                Assert.state(false, "type not found " + p.getType());
        }
        return null;
    }

    @RequestMapping("/chartData")
    public @ResponseBody
    Map<Integer, List<Number[]>> chartData(@RequestParam(value = "id[]", required = true) List<Integer> ids,
            @RequestParam(value = "tstart", required = true, defaultValue = "0") long timeStart,
            @RequestParam(value = "tend", required = true, defaultValue = "-1") long timeEnd, Model model) {
        //        log.debug("ids = {}, timeStart = {}", Arrays.toString(ids), timeStart);
        //        Iterable<Number[]> d = repo.getData(Arrays.asList(ids), timeStart);
        if (timeEnd == -1) {
            timeEnd = Instant.now().toEpochMilli();
        }
        Map<Integer, List<Number[]>> data = repo.getData(ids, timeStart, timeEnd);
        Number[] v = data.entrySet().stream().flatMap(e -> e.getValue().stream()).findFirst().orElse(new Number[]{0L, 0D});
        log.debug("ids = {}, timeStart = {}, timeEnd = {} -> size = {}, first {}, {}", ids, Instant.ofEpochMilli(timeStart),
                Instant.ofEpochMilli(timeEnd), data.size(), Instant.ofEpochMilli((long) v[0]), v[0]);
        return data;
    }

    @RequestMapping("/statsData")
    public @ResponseBody
    Map<String, Object[][]> statsData(@RequestParam(value = "id[]", required = true) List<Integer> ids,
            @RequestParam(value = "statType[]", required = true) List<StatsData.StatType> statTypes, Model model) {
        Map<String, Object[][]> data = new HashMap<>(ids.size());
        for (Integer id : ids) {
            for (StatsData.StatType i : statTypes) {
                data.put(String.format("%d_%s", id, i), monitor.get(id).getStats(i).getAverage());
            }
        }
        log.debug("ids = {}, statTypes = {}", ids, statTypes);
        return data;
    }

    @RequestMapping("/timeData")
    public @ResponseBody
    Map<Integer, List<Number[]>> timeData(@RequestParam(value = "id[]", required = true) List<Integer> ids,
            @RequestParam(value = "tstart", required = true, defaultValue = "0") long timeStart,
            @RequestParam(value = "tend", required = true, defaultValue = "-1") long timeEnd, Model model) {
        if (timeEnd == -1) {
            timeEnd = Instant.now().toEpochMilli();
        }
        Map<Integer, List<Number[]>> data = repo.getResponseTime(ids, timeStart, timeEnd);
        Number[] v = data.entrySet().stream().flatMap(e -> e.getValue().stream()).findFirst().orElse(new Number[]{0L, 0D});
        log.debug("ids = {}, timeStart = {}, timeEnd = {} -> size = {}, first {}, {}", ids, Instant.ofEpochMilli(timeStart),
                Instant.ofEpochMilli(timeEnd), data.size(), Instant.ofEpochMilli((long) v[0]), v[0]);
        return data;
    }

}
