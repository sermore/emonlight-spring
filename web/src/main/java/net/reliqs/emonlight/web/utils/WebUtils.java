package net.reliqs.emonlight.web.utils;

public class WebUtils {
    public static String wClass(boolean valid) {
        return "form-control form-control-sm" + (valid ? "is-invalid" : "is-valid'");
    }
}
