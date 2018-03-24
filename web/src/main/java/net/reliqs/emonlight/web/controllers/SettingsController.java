package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SettingsController {

    @Autowired
    private Settings settings;

    @RequestMapping("/list")
    public String list(Model model) {
        model.addAttribute("settings", settings);
        return "list";
    }
}
