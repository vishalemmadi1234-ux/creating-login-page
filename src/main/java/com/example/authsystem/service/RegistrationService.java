package com.example.authsystem.service;

import com.example.authsystem.dto.RegistrationForm;
import com.example.authsystem.entity.User;
import com.example.authsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class RegistrationService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return users.existsByEmailIgnoreCase(normalize(email));
    }

    @Transactional
    public void register(RegistrationForm form) {
        String email = normalize(form.getEmail());
        if (emailExists(email)) throw new IllegalArgumentException("Email already registered");
        if (!form.getPassword().equals(form.getConfirmPassword()))
            throw new IllegalArgumentException("Passwords do not match");
        if (form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Password is too long for BCrypt");

        User user = new User(form.getName().trim(), email,
                             passwordEncoder.encode(form.getPassword()));
        users.save(user);
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
