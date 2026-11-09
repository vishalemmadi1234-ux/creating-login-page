package com.example.authsystem.controller;

import com.example.authsystem.dto.RegistrationForm;
import com.example.authsystem.entity.User;
import com.example.authsystem.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {
    private final UserRepository users;
    public PageController(UserRepository users) { this.users = users; }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(name = "view", defaultValue = "login") String view, Model model) {
        model.addAttribute("activeView", ("register".equals(view) || "forgot".equals(view)) ? view : "login");
        model.addAttribute("registrationForm", new RegistrationForm());
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = users.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        model.addAttribute("user", user);
        return "dashboard";
    }
}
