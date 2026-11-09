package com.example.authsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String fromAddress;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${app.mail-from}") String fromAddress) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.fromAddress = fromAddress;
    }

    public void sendPasswordReset(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Reset your Climentos password");
        message.setText("We received a request to reset your Climentos password.\n\n"
            + "Open this link (expires in 15 minutes):\n"
            + baseUrl + "/reset-password?token=" + token + "\n\n"
            + "If you did not request this, you can ignore this email.\n");
        mailSender.send(message);
    }
}
