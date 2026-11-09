package com.example.authsystem.service;

import com.example.authsystem.dto.ResetPasswordForm;
import com.example.authsystem.entity.PasswordResetToken;
import com.example.authsystem.entity.User;
import com.example.authsystem.repository.PasswordResetTokenRepository;
import com.example.authsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class PasswordResetService {
    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder passwords;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder passwords, EmailService emailService) {
        this.users = users;
        this.tokens = tokens;
        this.passwords = passwords;
        this.emailService = emailService;
    }

    @Transactional
    public void requestLink(String email) {
        // Caller always displays the same response, whether or not the account exists.
        Optional<User> account = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT));
        if (account.isEmpty()) return;
        User user = account.get();
        Instant now = Instant.now();

        // Per-account cooldown. Add IP-based rate limiting before public deployment.
        var previous = tokens.findFirstByUserOrderByCreatedAtDesc(user);
        if (previous.isPresent() && previous.get().getCreatedAt().plusSeconds(60).isAfter(now))
            return;

        for (PasswordResetToken old : tokens.findAllByUserAndUsedAtIsNull(user)) {
            old.markUsed(now);
        }

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new PasswordResetToken(user, hash(rawToken), now));
        // If SMTP fails, the transaction rolls back. Never log raw reset tokens.
        emailService.sendPasswordReset(user.getEmail(), rawToken);
    }

    @Transactional(readOnly = true)
    public boolean isValid(String rawToken) {
        return lookup(rawToken).isPresent();
    }

    @Transactional
    public boolean resetPassword(String rawToken, ResetPasswordForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) return false;
        if (form.getPassword().length() < 8 || form.getPassword().length() > 64 ||
            form.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) return false;

        Optional<PasswordResetToken> found = lookup(rawToken);
        if (found.isEmpty()) return false;
        PasswordResetToken token = found.get();
        Instant now = Instant.now();
        token.getUser().setPasswordHash(passwords.encode(form.getPassword()));
        for (PasswordResetToken outstanding : tokens.findAllByUserAndUsedAtIsNull(token.getUser())) {
            outstanding.markUsed(now);
        }
        // Existing sessions are NOT globally revoked in this baseline implementation.
        return true;
    }

    private Optional<PasswordResetToken> lookup(String rawToken) {
        if (rawToken == null || !rawToken.matches("[A-Za-z0-9_-]{40,100}"))
            return Optional.empty();
        return tokens.findByTokenHash(hash(rawToken))
                .filter(token -> token.isValid(Instant.now()));
    }

    private String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
