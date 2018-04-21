package net.reliqs.emonlight.web.utils;

import net.reliqs.emonlight.commons.config.Settings;
import net.reliqs.emonlight.commons.config.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WebUtils {
    private static final Logger log = LoggerFactory.getLogger(WebUtils.class);


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

    public static String readApplicationStatus() {
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("sudo", "systemctl", "is-active", "emonlight-xbee-gw");
            Process p = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
        } catch (IOException e) {
            log.warn("error on getting application status", e);
        }
        return sb.toString();
    }

    public static boolean restartApplication() {
        try {
            ProcessBuilder pb = new ProcessBuilder("sudo", "systemctl", "restart", "emonlight-xbee-gw");
            Process p = pb.start();
            p.waitFor();
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            log.warn("error on application restart", e);
        }
        return false;
    }

}
