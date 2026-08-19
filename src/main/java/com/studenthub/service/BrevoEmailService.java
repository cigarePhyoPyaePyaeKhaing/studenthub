package com.studenthub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BrevoEmailService implements EmailService {
    private static final Logger LOGGER = Logger.getLogger(BrevoEmailService.class.getName());
    static final URI API_ENDPOINT = URI.create("https://api.brevo.com/v3/smtp/email");
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    interface Configuration {
        String value(String name);
    }

    interface Transport {
        int send(HttpRequest request) throws IOException, InterruptedException;
    }

    private final Transport transport;
    private final Configuration configuration;
    private final ObjectMapper objectMapper;

    public BrevoEmailService() {
        this(new JavaHttpTransport(), System::getenv, new ObjectMapper());
    }

    BrevoEmailService(Transport transport, Configuration configuration, ObjectMapper objectMapper) {
        this.transport = transport;
        this.configuration = configuration;
        this.objectMapper = objectMapper;
    }

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

    private void send(String recipient, String subject, String textContent) throws EmailServiceException {
        String apiKey = required("BREVO_API_KEY");
        String fromEmail = required("BREVO_FROM_EMAIL");
        String configuredName = configuration.value("BREVO_FROM_NAME");
        String fromName = configuredName == null || configuredName.isBlank() ? "StudentHub" : configuredName.trim();

        HttpRequest request = HttpRequest.newBuilder(API_ENDPOINT)
                .timeout(REQUEST_TIMEOUT)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json(fromEmail, fromName, recipient, subject, textContent)))
                .build();
        try {
            int status = transport.send(request);
            if (status < 200 || status >= 300) {
                throw new EmailServiceException("Brevo email API rejected the request with HTTP " + status + ".", null);
            }
            LOGGER.info((subject.startsWith("Reset") ? "Password reset" : "Verification")
                    + " email request accepted by provider (HTTP " + status + ").");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EmailServiceException("Brevo email API request was interrupted.", exception);
        } catch (IOException exception) {
            throw new EmailServiceException("Brevo email API is temporarily unavailable.", exception);
        }
    }

    private String json(String fromEmail, String fromName, String recipient, String subject, String textContent)
            throws EmailServiceException {
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", recipient)),
                "subject", subject,
                "textContent", textContent);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException exception) {
            throw new EmailServiceException("Email request could not be prepared.", exception);
        }
    }

    private String required(String name) throws EmailServiceException {
        String value = configuration.value(name);
        if (value == null || value.isBlank()) {
            throw new EmailServiceException("Required email configuration is missing: " + name, null);
        }
        return value.trim();
    }

    private static final class JavaHttpTransport implements Transport {
        private final HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        @Override
        public int send(HttpRequest request) throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String safeBody = response.body() == null ? "" : response.body().replaceAll("[\\r\\n]", " ")
                        .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+", "[redacted-email]")
                        .replaceAll("(?<!\\d)\\d{6}(?!\\d)", "[redacted-code]");
                if (safeBody.length() > 500) safeBody = safeBody.substring(0, 500);
                LOGGER.warning("Brevo email API failure HTTP " + response.statusCode() + ": " + safeBody);
            }
            return response.statusCode();
        }
    }
}
