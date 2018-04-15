package net.reliqs.emonlight.web.utils;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;

import javax.servlet.http.HttpSession;

public class WebUtils {
    public static String wClass(boolean valid) {
        return "form-control form-control-sm" + (valid ? "is-invalid" : "is-valid'");
    }

    public static Settings loadSettings(SettingsService settingsService, HttpSession session) {
        Settings settings = (Settings) session.getAttribute("settings");
        if (settings == null) {
            settings = settingsService.loadAndInitialize();
            session.setAttribute("settings", settings);
        }
        return settings;
    }

}
