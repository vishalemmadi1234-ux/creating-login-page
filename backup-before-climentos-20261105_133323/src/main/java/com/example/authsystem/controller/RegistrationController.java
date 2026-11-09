
package com.example.authsystem.controller;

import com.example.authsystem.dto.RegistrationForm;
import com.example.authsystem.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    // Constructor Dependency Injection
    public RegistrationController(
            RegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    // Display registration page
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {

        model.addAttribute(
                "registrationForm",
                new RegistrationForm()
        );

        return "register";
    }

    // Handle registration submission
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationForm")
            RegistrationForm form,
            BindingResult result) {

        // Check passwords match
        if (!Objects.equals(
                form.getPassword(),
                form.getConfirmPassword())) {

            result.rejectValue(
                    "confirmPassword",
                    "error.confirmPassword",
                    "Passwords do not match"
            );
        }

        // Check validation errors
        if (result.hasErrors()) {
            return "register";
        }

        // Check duplicate email
        if (registrationService.emailExists(form.getEmail())) {

            result.rejectValue(
                    "email",
                    "error.email",
                    "Email already registered"
            );

            return "register";
        }

        try {
            // Save registered user
            registrationService.register(form);

        } catch (IllegalArgumentException |
                 DataIntegrityViolationException ex) {

            result.rejectValue(
                    "email",
                    "error.email",
                    "Email already registered"
            );

            return "register";
        }

        // Registration successful
        return "redirect:/register?success";
    }
}

