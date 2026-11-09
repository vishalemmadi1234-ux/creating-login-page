package com.example.authsystem.repository;

import com.example.authsystem.entity.PasswordResetToken;
import com.example.authsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    Optional<PasswordResetToken> findFirstByUserOrderByCreatedAtDesc(User user);
    List<PasswordResetToken> findAllByUserAndUsedAtIsNull(User user);
}
