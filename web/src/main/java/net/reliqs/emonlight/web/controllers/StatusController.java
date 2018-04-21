package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.web.utils.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/settings/*")
public class StatusController {
    private static final Logger log = LoggerFactory.getLogger(StatusController.class);

    @GetMapping("status")
    public String show(Model model) {
        String status = WebUtils.readApplicationStatus();
        model.addAttribute("status", status.isEmpty() ? "unknown" : status);
        model.addAttribute("statusClass", "active".equals(status) ? "alert-success" : "alert-warning");
        return "settings/status";
    }

    @PostMapping(value = "status", params = "restart")
    public String restartApplication(String restart, final RedirectAttributes attrs, Model model) {
        log.debug("restart application");
        if (WebUtils.restartApplication()) {
            attrs.addFlashAttribute("message", "Application restarted.");
            attrs.addFlashAttribute("messageClass", "alert-success");
        } else {
            attrs.addFlashAttribute("message", String.format("Application restart failed."));
            attrs.addFlashAttribute("messageClass", "alert-danger");
        }
        return "redirect:status";
    }

}
