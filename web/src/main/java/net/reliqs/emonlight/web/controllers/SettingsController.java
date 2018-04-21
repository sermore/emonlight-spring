package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.*;
import net.reliqs.emonlight.web.services.FileRepository;
import net.reliqs.emonlight.web.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/settings/*")
public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private FileRepository repo;

    private Settings loadSettings(HttpSession session) {
        return WebUtils.loadSettings(settingsService, session);
    }

    @GetMapping(value = "restore", params = "name")
    public String restore(String name, Model model, HttpSession session, final RedirectAttributes attrs) {
        log.debug("restore {}", name);
        repo.checkoutAndCommit(name);
        session.removeAttribute("settings");
        attrs.addFlashAttribute("message", String.format("Successfully restored version %s.", name));
        attrs.addFlashAttribute("messageClass", "alert-success");
        return "redirect:edit";
    }

    @GetMapping("edit")
    public String edit(Model model, HttpSession session) {
        log.debug("edit {}", model);
        //        Settings settings = (Settings) session.getAttribute("settings");
        Settings settings = loadSettings(session);
        model.addAttribute("settings", settings);
        //        model.addAttribute("commitMessage", "");
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "reset")
    public String reset(@ModelAttribute("commitMessage") String commitMessage, final RedirectAttributes attrs) {
        log.debug("reset {}");
        attrs.addFlashAttribute("commitMessage", commitMessage);
        attrs.addFlashAttribute("message", "Changes discarded.");
        attrs.addFlashAttribute("messageClass", "alert-info");
        return "redirect:edit";
    }

    @PostMapping(value = "edit", params = "restart")
    public String restartApplication(@Valid Settings settings, BindingResult bindingResult, String restart,
            @ModelAttribute("commitMessage") String commitMessage, final RedirectAttributes attrs, Model model) {
        log.debug("restart application");
        if (WebUtils.restartApplication()) {
            attrs.addFlashAttribute("commitMessage", commitMessage);
            attrs.addFlashAttribute("message", "Application restarted.");
            attrs.addFlashAttribute("messageClass", "alert-success");
            return "redirect:edit";
        } else {
            model.addAttribute("message", String.format("Application restart failed."));
            model.addAttribute("messageClass", "alert-danger");
            return "settings/edit";
        }
    }

    @PostMapping(value = "edit")
    public String save(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            final RedirectAttributes attrs, Model model, HttpSession session) {
        //        model.addAttribute("settings", settings);
        log.debug("save settings {}, {}", settings, model);
        if (bindingResult.hasErrors()) {
            log.debug("errors {}", bindingResult.getAllErrors());
            //            model.addAttribute("commitMessage", commitMessage);
            model.addAttribute("message", "Unable to save Settings due to presence of validation failures.");
            model.addAttribute("messageClass", "alert-danger");
            return "settings/edit";
        }
        settings.init();
        settingsService.save(settings);
        String commitMsg = repo.commitWithDiff(commitMessage);
        if (commitMsg != null) {
            attrs.addFlashAttribute("message", "Settings saved.\n" + commitMessage);
            attrs.addFlashAttribute("messageClass", "alert-success");
            session.setAttribute("settings", settings);
            return "redirect:edit";
        } else {
            //            model.addAttribute("commitMessage", commitMessage);
            model.addAttribute("message", "No changes identified.");
            model.addAttribute("messageClass", "alert-info");
            return "settings/edit";
        }
    }

    @PostMapping(value = "edit", params = "addNode")
    public String addNode(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage, Model model) {
        Node node = settings.addNewNode();
        log.debug("add new node {}, {}", node, model);
        model.addAttribute("message", String.format("Added new node '%s'", node.getName()));
        model.addAttribute("messageClass", "alert-info");
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "removeNode")
    public String removeNode(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            @RequestParam(value = "removeNode", required = true) Integer nodeIndex, Model model) {
        if (nodeIndex != null) {
            log.debug("remove nodeIndex {}", nodeIndex);
            Node node = settings.removeNode(nodeIndex);
            // FIXME find the way to remove validation messages for the removed items
            if (node != null) {
                model.addAttribute("message", String.format("Removed node '%s'", node.getName()));
                model.addAttribute("messageClass", "alert-info");
                return "settings/edit";
            }
        }
        log.warn("node not found: nodeIndex {}", nodeIndex);
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "addProbe")
    public String addProbe(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            @RequestParam(value = "addProbe", required = true) Integer nodeIndex, Model model) {
        if (nodeIndex != null) {
            log.debug("add probe to nodeIndex {}", nodeIndex);
            Probe probe = settings.addNewProbe(nodeIndex);
            if (probe != null) {
                model.addAttribute("message", String.format("Added probe '%s' to node '%s'", probe.getName(), probe.getNode().getName()));
                model.addAttribute("messageClass", "alert-info");
                return "settings/edit";
            }
        }
        log.warn("node not found: nodeIndex {}", nodeIndex);
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "removeProbe")
    public String removeProbe(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            @RequestParam(value = "removeProbe", required = true) String nodeProbeIndex, Model model) {
        if (nodeProbeIndex != null && !nodeProbeIndex.isEmpty()) {
            String[] split = nodeProbeIndex.split(",");
            Integer nodeIndex = Integer.valueOf(split[0]);
            Integer probeIndex = Integer.valueOf(split[1]);
            log.debug("remove nodeIndex {}, probeIndex {}, {}", nodeIndex, probeIndex, bindingResult);
            // FIXME find the way to remove validation messages for the removed items
            if (nodeIndex != null && probeIndex != null) {
                Probe probe = settings.removeProbe(nodeIndex, probeIndex);
                if (probe != null) {
                    Node node = settings.getNodes().get(nodeIndex);
                    model.addAttribute("message", String.format("Removed probe '%s' from node '%s'", probe.getName(), node.getName()));
                    model.addAttribute("messageClass", "alert-info");
                    return "settings/edit";
                }
            }
        }
        log.warn("node or probe not found: {}", nodeProbeIndex);
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "addServer")
    public String addServer(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            Model model) {
        Server s = settings.addNewServer();
        log.debug("add new server {}", s);
        model.addAttribute("message", String.format("Added new REST endpoint '%s'", s.getName()));
        model.addAttribute("messageClass", "alert-info");
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "removeServer")
    public String removeServer(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            @RequestParam(value = "removeServer", required = true) Integer serverIndex, Model model) {
        if (serverIndex != null) {
            log.debug("remove serverIndex {}", serverIndex);
            Server s = settings.removeServer(serverIndex);
            if (s != null) {
                model.addAttribute("message", String.format("Removed REST endpoint '%s'", s.getName()));
                model.addAttribute("messageClass", "alert-info");
                return "settings/edit";
            }
        }
        log.warn("server not found: serverIndex {}", serverIndex);
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "addServerMap")
    public String addServerMap(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            @RequestParam(value = "addServerMap", required = true) Integer serverIndex, Model model) {
        if (serverIndex != null) {
            log.debug("add serverMap to serverIndex {}", serverIndex);
            ServerMap sm = settings.addNewServerMap(serverIndex);
            if (sm != null) {
                Server s = settings.getServers().get(serverIndex);
                model.addAttribute("message", String.format("Added mapping to REST endpoint '%s'", s.getName()));
                model.addAttribute("messageClass", "alert-info");
                return "settings/edit";
            }
        }
        log.warn("server not found: serverIndex {}", serverIndex);
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "removeServerMap")
    public String removeServerMap(@Valid Settings settings, BindingResult bindingResult, @ModelAttribute("commitMessage") String commitMessage,
            @RequestParam(value = "removeServerMap", required = true) String serverMapIndex, Model model) {
        if (serverMapIndex != null && !serverMapIndex.isEmpty()) {
            String[] split = serverMapIndex.split(",");
            Integer serverIndex = Integer.valueOf(split[0]);
            Integer mapIndex = Integer.valueOf(split[1]);
            log.debug("remove serverIndex {}, mapIndex {}", serverIndex, mapIndex);
            if (serverIndex != null && mapIndex != null) {
                ServerMap sm = settings.removeServerMap(serverIndex, mapIndex);
                if (sm != null) {
                    Server s = settings.getServers().get(serverIndex);
                    model.addAttribute("message", String.format("Removed mapping from REST endpoint '%s'", s.getName()));
                    model.addAttribute("messageClass", "alert-info");
                    return "settings/edit";
                }
            }
        }
        log.warn("server or map not found: {}", serverMapIndex);
        return "settings/edit";
    }

    @ModelAttribute("opModes")
    public Node.OpMode[] populateOpModes() {
        return Node.OpMode.values();
    }

    @ModelAttribute("probeTypes")
    public Probe.Type[] populateProbeTypes() {
        return Probe.Type.options();
    }

    @ModelAttribute("probeList")
    public List<Probe> populateProbeList(HttpSession session) {
        Settings settings = loadSettings(session);
        return settings.getProbes().collect(Collectors.toList());
    }

}
