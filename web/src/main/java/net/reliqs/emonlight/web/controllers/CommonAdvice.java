package net.reliqs.emonlight.web.controllers;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import net.reliqs.emonlight.web.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpSession;

@ControllerAdvice(
        assignableTypes = {ChartController.class, HistoryController.class, HomeController.class, StatusController.class, ChartController.class})
public class CommonAdvice {

    @Autowired
    private SettingsService settingsService;

    private Settings loadSettings(HttpSession session) {
        return WebUtils.loadSettings(settingsService, session);
    }

    @ModelAttribute("settings")
    public Settings settings(HttpSession session) {
        return loadSettings(session);
    }

}
