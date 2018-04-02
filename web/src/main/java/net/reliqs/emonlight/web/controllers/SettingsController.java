package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Node;
import net.reliqs.emonlight.commons.config.Probe;
import net.reliqs.emonlight.commons.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/settings/*")
public class SettingsController {
    private static final Logger log = LoggerFactory.getLogger(SettingsController.class);

    @Autowired
    private Settings settings;

    @GetMapping("edit")
    public String edit(Model model) {
        log.debug("edit {}", model);
        model.addAttribute("settings", settings);
        return "settings/edit";
    }

    @PostMapping(value = "edit")
    public String save(@Valid Settings settings, BindingResult bindingResult, final RedirectAttributes attrs, Model model) {
        //        model.addAttribute("settings", settings);
        log.debug("save settings {}", settings);
        if (bindingResult.hasErrors()) {
            log.debug("errors {}", bindingResult.getAllErrors());
            model.addAttribute("message", "Unable to save Settings due to presence of validation failures.");
            model.addAttribute("messageClass", "alert-danger");
            return "settings/edit";
        }
        attrs.addFlashAttribute("message", "Settings saved.");
        attrs.addFlashAttribute("messageClass", "alert-success");
        return "redirect:edit";
    }

    @PostMapping(value = "edit", params = "addNode")
    public String addNode(@Valid Settings settings, BindingResult bindingResult, Model model) {
        Node node = settings.addNewNode();
        log.debug("add new node {}", node);
        model.addAttribute("message", String.format("Added new node '%s'", node.getName()));
        model.addAttribute("messageClass", "alert-info");
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "removeNode")
    public String removeNode(@Valid Settings settings, BindingResult bindingResult,
            @RequestParam(value = "removeNode", required = true) Integer nodeIndex, Model model) {
        if (nodeIndex != null) {
            log.debug("remove nodeIndex {}", nodeIndex);
            Node node = settings.removeNode(nodeIndex);
            if (node != null) {
                model.addAttribute("message", String.format("Removed node '%s'", node.getName()));
                model.addAttribute("messageClass", "alert-info");
            }
        } else {
            log.warn("node not found: nodeIndex {}", nodeIndex);
        }
        return "settings/edit";
    }

    @PostMapping(value = "edit", params = "addProbe")
    public String addNode(@Valid Settings settings, BindingResult bindingResult, @RequestParam(value = "addProbe", required = true) Integer nodeIndex,
            Model model) {
        if (nodeIndex != null) {
            log.debug("add probe to nodeIndex {}", nodeIndex);
            Probe probe = settings.addNewProbe(nodeIndex);
            if (probe != null) {
                model.addAttribute("message", String.format("Added probe '%s' to node '%s'", probe.getName(), probe.getNode().getName()));
                model.addAttribute("messageClass", "alert-info");
            }
        } else {
            log.warn("node not found: nodeIndex {}", nodeIndex);
        }
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

}
