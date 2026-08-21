package com.studenthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class BrevoEmailServiceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String SECRET = "test-api-key-never-log";

    @Test void verificationEmailUsesEndpointHeadersSenderRecipientAndOtp() throws Exception {
        CapturingTransport transport = new CapturingTransport(201);
        service(transport).sendVerificationOtp("student@example.com", "Ma \"Nandar\"", "123456");

        HttpRequest request = transport.request;
        assertEquals(BrevoEmailService.API_ENDPOINT, request.uri());
        assertEquals("POST", request.method());
        assertEquals("application/json", request.headers().firstValue("accept").orElseThrow());
        assertEquals("application/json", request.headers().firstValue("content-type").orElseThrow());
        assertEquals(SECRET, request.headers().firstValue("api-key").orElseThrow());
        assertFalse(request.toString().contains(SECRET));

        JsonNode body = JSON.readTree(body(request));
        assertEquals("sender@example.com", body.at("/sender/email").asText());
        assertEquals("StudentHub", body.at("/sender/name").asText());
        assertEquals("student@example.com", body.at("/to/0/email").asText());
        assertEquals("Verify your StudentHub email", body.get("subject").asText());
        assertTrue(body.get("textContent").asText().contains("123456"));
    }

    @Test void resetEmailPreservesPasswordResetPurpose() throws Exception {
        CapturingTransport transport = new CapturingTransport(202);
        service(transport).sendPasswordResetOtp("reset@example.com", "Ko Min", "654321");
        JsonNode body = JSON.readTree(body(transport.request));
        assertEquals("Reset your StudentHub password", body.get("subject").asText());
        assertTrue(body.get("textContent").asText().contains("654321"));
        assertTrue(body.get("textContent").asText().contains("10 minutes"));
    }

    @Test void jacksonSafelyEscapesDynamicJsonValues() throws Exception {
        CapturingTransport transport = new CapturingTransport(201);
        service(transport).sendVerificationOtp("student@example.com", "Name \"quoted\"\nMyanmar မြန်မာ", "123456");
        String raw = body(transport.request);
        assertTrue(raw.contains("\\\"quoted\\\""));
        assertTrue(raw.contains("\\nMyanmar"));
        assertDoesNotThrow(() -> JSON.readTree(raw));
    }

    @Test void anyTwoHundredResponseIsSuccessful() {
        assertDoesNotThrow(() -> service(new CapturingTransport(200))
                .sendVerificationOtp("a@example.com", "A", "123456"));
        assertDoesNotThrow(() -> service(new CapturingTransport(299))
                .sendPasswordResetOtp("a@example.com", "A", "123456"));
    }

    @Test void unauthorizedResponsesAreClassifiedSeparately() {
        for (int status : new int[]{401, 403}) {
            EmailServiceException error = assertThrows(EmailServiceException.class,
                    () -> service(new CapturingTransport(status, "{\"message\":\"unauthorized\"}")) .sendVerificationOtp(
                            "a@example.com", "A", "123456"));
            assertEquals(EmailServiceException.Reason.UNAUTHORIZED, error.reason());
            assertEquals(status, error.providerStatus());
        }
    }

    @Test void otherClientAndServerResponsesAreProviderFailures() {
        for (int status : new int[]{400, 500}) {
            EmailServiceException error = assertThrows(EmailServiceException.class,
                    () -> service(new CapturingTransport(status, "{\"message\":\"provider rejected reset@example.com code 123456\"}"))
                            .sendPasswordResetOtp("a@example.com", "A", "123456"));
            assertEquals(EmailServiceException.Reason.PROVIDER, error.reason());
            assertEquals(status, error.providerStatus());
            assertFalse(error.getMessage().contains(SECRET));
            assertFalse(error.getMessage().contains("123456"));
        }
    }

    @Test void providerResponseSanitizationRemovesSensitiveValues() {
        String sanitized = BrevoEmailService.sanitizeProviderResponse(
                "email=student@example.com otp=123456 api-key=" + SECRET + " password=hunter2");
        assertFalse(sanitized.contains("student@example.com"));
        assertFalse(sanitized.contains("123456"));
        assertFalse(sanitized.contains(SECRET));
        assertFalse(sanitized.contains("hunter2"));
    }

    @Test void networkFailureIsGenericAndDoesNotLeakKeyOrOtp() {
        BrevoEmailService.Transport failing = request -> { throw new IOException("socket unavailable"); };
        EmailServiceException error = assertThrows(EmailServiceException.class,
                () -> service(failing).sendVerificationOtp("a@example.com", "A", "123456"));
        assertEquals("Brevo email API is temporarily unavailable.", error.getMessage());
        assertFalse(error.getMessage().contains(SECRET));
        assertFalse(error.getMessage().contains("123456"));
    }

    @Test void interruptionRestoresThreadInterruptFlag() {
        BrevoEmailService.Transport interrupted = request -> { throw new InterruptedException("interrupted"); };
        try {
            EmailServiceException error = assertThrows(EmailServiceException.class,
                    () -> service(interrupted).sendPasswordResetOtp("a@example.com", "A", "123456"));
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(error.getMessage().contains(SECRET));
        } finally {
            Thread.interrupted();
        }
    }

    @Test void missingRequiredConfigurationFailsAndSenderNameDefaultsSafely() throws Exception {
        CapturingTransport transport = new CapturingTransport(201);
        BrevoEmailService missing = new BrevoEmailService(transport, name -> null, JSON);
        assertThrows(EmailServiceException.class,
                () -> missing.sendVerificationOtp("a@example.com", "A", "123456"));

        service(transport).sendVerificationOtp("a@example.com", "A", "123456");
        assertEquals("StudentHub", JSON.readTree(body(transport.request)).at("/sender/name").asText());
    }

    private BrevoEmailService service(BrevoEmailService.Transport transport) {
        Map<String,String> values = Map.of("BREVO_API_KEY", SECRET, "BREVO_FROM_EMAIL", "sender@example.com");
        return new BrevoEmailService(transport, values::get, JSON);
    }

    private static String body(HttpRequest request) throws Exception {
        HttpRequest.BodyPublisher publisher = request.bodyPublisher().orElseThrow();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        CompletableFuture<Void> complete = new CompletableFuture<>();
        publisher.subscribe(new Flow.Subscriber<>() {
            public void onSubscribe(Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
            public void onNext(ByteBuffer item) { byte[] bytes=new byte[item.remaining()];item.get(bytes);output.writeBytes(bytes); }
            public void onError(Throwable throwable) { complete.completeExceptionally(throwable); }
            public void onComplete() { complete.complete(null); }
        });
        complete.get();
        return output.toString(StandardCharsets.UTF_8);
    }

    private static final class CapturingTransport implements BrevoEmailService.Transport {
        private final int status;
        private final String responseBody;
        private HttpRequest request;
        private CapturingTransport(int status) { this(status, "{}"); }
        private CapturingTransport(int status, String responseBody) { this.status=status; this.responseBody=responseBody; }
        public BrevoEmailService.DeliveryResponse send(HttpRequest request) {
            this.request=request;
            return new BrevoEmailService.DeliveryResponse(status, responseBody);
        }
    }
}
