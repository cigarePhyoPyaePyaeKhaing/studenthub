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
        DeliveryResponse send(HttpRequest request) throws IOException, InterruptedException;
    }

    record DeliveryResponse(int statusCode, String responseBody) {}

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
            DeliveryResponse response = transport.send(request);
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                LOGGER.warning("Brevo email delivery failed. status=" + status
                        + ", response=" + sanitizeProviderResponse(response.responseBody()));
                EmailServiceException.Reason reason = status == 401 || status == 403
                        ? EmailServiceException.Reason.UNAUTHORIZED : EmailServiceException.Reason.PROVIDER;
                throw new EmailServiceException("Brevo email API rejected the request with HTTP " + status + ".", null, reason, status);
            }
            LOGGER.info((subject.startsWith("Reset") ? "Password reset" : "Verification")
                    + " email request accepted by provider (HTTP " + status + ").");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EmailServiceException("Brevo email API request was interrupted.", exception,
                    EmailServiceException.Reason.INTERRUPTED, null);
        } catch (IOException exception) {
            throw new EmailServiceException("Brevo email API is temporarily unavailable.", exception,
                    EmailServiceException.Reason.NETWORK, null);
        }
    }

    static String sanitizeProviderResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) return "[empty]";
        String sanitized = responseBody.replaceAll("[\\r\\n]", " ")
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+", "[redacted-email]")
                .replaceAll("(?<!\\d)\\d{6}(?!\\d)", "[redacted-code]")
                .replaceAll("(?i)(api[-_ ]?key|password|authorization)\\s*[:=]\\s*[^,} ]+", "$1=[redacted]");
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
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
            throw new EmailServiceException("Email request could not be prepared.", exception,
                    EmailServiceException.Reason.PREPARATION, null);
        }
    }

    private String required(String name) throws EmailServiceException {
        String value = configuration.value(name);
        if (value == null || value.isBlank()) {
            throw new EmailServiceException("Required email configuration is missing: " + name, null,
                    EmailServiceException.Reason.CONFIGURATION, null);
        }
        return value.trim();
    }

    private static final class JavaHttpTransport implements Transport {
        private final HttpClient client = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        @Override
        public DeliveryResponse send(HttpRequest request) throws IOException, InterruptedException {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new DeliveryResponse(response.statusCode(), response.body());
        }
    }
}
