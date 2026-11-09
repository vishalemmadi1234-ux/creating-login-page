package com.example.authsystem.controller;

import com.example.authsystem.dto.RegistrationForm;
import com.example.authsystem.dto.ResetPasswordForm;
import com.example.authsystem.service.PasswordResetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

@Controller
public class PasswordResetController {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private final PasswordResetService resets;

    public PasswordResetController(PasswordResetService resets) { this.resets = resets; }

    @GetMapping("/forgot-password")
    public String forgot() { return "redirect:/login?view=forgot"; }

    @PostMapping("/forgot-password")
    public String request(@RequestParam("email") String email) {
        // Same response regardless of whether the email is registered.
        if (email != null && email.length() <= 255) {
            try {
                resets.requestLink(email);
            } catch (MailException ex) {
                log.warn("Password reset delivery failed; check SMTP configuration");
            }
        }
        return "redirect:/login?view=forgot&sent";
    }

    @GetMapping("/reset-password")
    public String resetPage(@RequestParam(name = "token", defaultValue = "") String token, Model model) {
        if (!resets.isValid(token)) return invalid(model);
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("activeView", "reset");
        return "login";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@RequestParam(name = "token", defaultValue = "") String token,
                              @Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                              BindingResult errors, Model model) {
        model.addAttribute("token", token);
        if (!resets.isValid(token)) return invalid(model);
        if (!Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (errors.hasErrors()) {
            model.addAttribute("registrationForm", new RegistrationForm());
            model.addAttribute("activeView", "reset");
            return "login";
        }
        if (!resets.resetPassword(token, form)) return invalid(model);
        return "redirect:/login?reset";
    }

    private String invalid(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        model.addAttribute("activeView", "invalid");
        return "login";
    }
}
