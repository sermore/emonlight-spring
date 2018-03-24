package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private Settings settings;

    @RequestMapping("/")
    public String home(Model model) {
        log.debug("settings {}", settings);
        model.addAttribute("settings", settings);
        return "index";
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
