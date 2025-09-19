package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @Value("${server.port:0}")
    private int port;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/port")
    public int getPort() {
        return port;
    }

    @GetMapping("/profile")
    public String getActiveProfile() {
        return "Active profile: " + activeProfile;
    }
}
