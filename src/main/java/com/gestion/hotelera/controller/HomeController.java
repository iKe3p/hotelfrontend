package com.gestion.hotelera.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String showHomePage(Model model, Authentication authentication) {
        boolean isLoggedIn = false;
        String username = "";
        try {
            if (authentication != null && authentication.isAuthenticated()
                    && !(authentication instanceof AnonymousAuthenticationToken)) {
                isLoggedIn = true;
                username = authentication.getName();
            }
        } catch (Exception e) {
            isLoggedIn = false;
            username = "";
        }
        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("username", username);
        return "index";
    }
}