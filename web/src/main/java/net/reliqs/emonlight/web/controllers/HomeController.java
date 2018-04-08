package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.web.services.ProbeMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("settings")
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);


    @Autowired
    private SettingsService settingsService;
    @Autowired
    private ProbeMonitor monitor;

    @ModelAttribute("settings")
    public Settings getSettings() {
        return settingsService.loadAndInitialize();
    }


    @GetMapping("/")
    public String home(Model model) {
        //        log.debug("settings {}", settings);
        //        model.addAttribute("settings", settings);
        return "index";
    }

    @GetMapping("/list")
    public String list(Model model) {
        //        model.addAttribute("settings", settings);
        model.addAttribute("monitor", monitor);
        return "list";
    }


    @RequestMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
    @RequestMapping("/greeting")
    public String greeting() {
        return "Greetings from Spring Boot!";
    }
}
