package com.example.authsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordForm {
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 64, message = "Use 8–64 characters")
    private String password;

    @NotBlank(message = "Please confirm your new password")
    private String confirmPassword;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
