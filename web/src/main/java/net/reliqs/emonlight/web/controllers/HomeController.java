package net.reliqs.emonlight.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {
    @RequestMapping("/")
    public String home() {

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
