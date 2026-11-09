
package com.example.authsystem.service;

import com.example.authsystem.dto.RegistrationForm;
import com.example.authsystem.entity.User;
import com.example.authsystem.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor Dependency Injection
    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Check if email already exists
    public boolean emailExists(String email) {

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        return userRepository
                .existsByEmailIgnoreCase(normalizedEmail);
    }

    // Register a new user
    public void register(RegistrationForm form) {

        String name = form.getName().trim();

        String email = form.getEmail().trim()
                .toLowerCase(Locale.ROOT);

        // Prevent duplicate registration
        if (emailExists(email)) {
            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        // Hash the password
        String hashedPassword =
                passwordEncoder.encode(form.getPassword());

        // Create User object
        User user = new User(
                name,
                email,
                hashedPassword
        );

        // Save user in MySQL
        userRepository.save(user);
    }
}

