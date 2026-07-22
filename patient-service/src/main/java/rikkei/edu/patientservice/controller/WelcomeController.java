package rikkei.edu.patientservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class WelcomeController {
    @Value("${app.welcome:Chao mung tam thoi}")
    private String welcomeMsg;

    @GetMapping("/welcome")
    public String getWelcomeMessage() {
        return welcomeMsg;
    }
}
