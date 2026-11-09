package com.example.authsystem.controller;

import com.example.authsystem.dto.RegistrationForm;
import com.example.authsystem.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Controller
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String form() { return "redirect:/login?view=register"; }

    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                         BindingResult result, Model model) {
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (form.getPassword() != null &&
            form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            result.rejectValue("password", "tooLong", "Password is too long for BCrypt");
        }
        if (result.hasErrors()) return showForm(model);
        if (registrationService.emailExists(form.getEmail())) {
            result.rejectValue("email", "duplicate", "Email already registered");
            return showForm(model);
        }
        try {
            registrationService.register(form);
        } catch (DataIntegrityViolationException ex) {
            result.rejectValue("email", "duplicate", "Email already registered");
            return showForm(model);
        } catch (IllegalArgumentException ex) {
            result.reject("registration", ex.getMessage());
            return showForm(model);
        }
        return "redirect:/login?registered";
    }

    private String showForm(Model model) {
        model.addAttribute("activeView", "register");
        return "login";
    }
}
