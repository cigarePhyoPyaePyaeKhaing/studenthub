package com.studenthub.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class BrevoEmailService implements EmailService {
    @Override
    public void sendVerificationOtp(String recipient, String fullName, String otp)
            throws EmailServiceException {
        send(recipient, "Verify your StudentHub email",
                message(fullName, "Your verification code is:", otp,
                        "If you did not create a StudentHub account, ignore this email."));
    }

    @Override
    public void sendPasswordResetOtp(String recipient, String fullName, String otp)
            throws EmailServiceException {
        send(recipient, "Reset your StudentHub password",
                message(fullName, "Your password reset code is:", otp,
                        "If you did not request a password reset, ignore this email."));
    }

    private String message(String fullName, String purpose, String otp, String warning) {
        return "Hello " + fullName + ",\n\nStudentHub\nUniversity of Information Technology\n\n"
                + purpose + "\n\n" + otp + "\n\nThis code expires in 10 minutes.\n\n" + warning;
    }

    private void send(String recipient, String subject, String content) throws EmailServiceException {
        String host = required("BREVO_SMTP_HOST");
        String port = required("BREVO_SMTP_PORT");
        String username = required("BREVO_SMTP_USERNAME");
        String password = required("BREVO_SMTP_PASSWORD");
        String fromEmail = required("BREVO_FROM_EMAIL");
        String fromName = required("BREVO_FROM_NAME");

        if (!"smtp-relay.brevo.com".equalsIgnoreCase(host.trim())) {
            throw new EmailServiceException(
                    "BREVO_SMTP_HOST must be smtp-relay.brevo.com for the Brevo SMTP relay.", null);
        }
        if (!"587".equals(port.trim())) {
            throw new EmailServiceException("BREVO_SMTP_PORT must be 587 for STARTTLS.", null);
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage email = new MimeMessage(session);
            // BREVO_FROM_EMAIL must be a sender address verified in the Brevo account.
            email.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            email.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            email.setSubject(subject, StandardCharsets.UTF_8.name());
            email.setText(content, StandardCharsets.UTF_8.name());
            Transport.send(email);
        } catch (MessagingException | java.io.UnsupportedEncodingException exception) {
            throw new EmailServiceException("Transactional email could not be sent.", exception);
        }
    }

    private String required(String name) throws EmailServiceException {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new EmailServiceException("Required email configuration is missing: " + name, null);
        }
        return value;
    }
}
