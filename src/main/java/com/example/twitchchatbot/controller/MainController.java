package com.example.twitchchatbot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Value("${spring.profiles.active:prod}")
    private String profile;

    @Value("${botDevUsername:bot}")
    private String botDevUsername;

    @Value("${botDevOAuth:bot}")
    private String botDevOauth;

    @Value("${botDevChannels:bot}")
    private String botDevChannels;

    @GetMapping
    public String mainPage(Model model) {
        boolean isDevMode = true;
        if(profile.equals("prod")) {
            isDevMode = false;
        }
        model.addAttribute("isDevMode", isDevMode);
        model.addAttribute("botDevUsername", botDevUsername);
        model.addAttribute("botDevOauth", botDevOauth);
        model.addAttribute("botDevChannels", botDevChannels);
        return "index";
    }
}
