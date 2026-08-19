package com.studenthub.service;

public class EmailServiceException extends Exception {
    public enum Reason { CONFIGURATION, UNAUTHORIZED, PROVIDER, NETWORK, INTERRUPTED, PREPARATION, UNKNOWN }
    private final Reason reason;
    private final Integer providerStatus;

    public EmailServiceException(String message, Throwable cause) {
        this(message, cause, Reason.UNKNOWN, null);
    }

    public EmailServiceException(String message, Throwable cause, Reason reason, Integer providerStatus) {
        super(message, cause);
        this.reason = reason;
        this.providerStatus = providerStatus;
    }

    public Reason reason() { return reason; }
    public Integer providerStatus() { return providerStatus; }
}
