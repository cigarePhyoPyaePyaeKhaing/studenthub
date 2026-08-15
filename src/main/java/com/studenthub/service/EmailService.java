package com.studenthub.service;

public interface EmailService {
    void sendVerificationOtp(String recipient, String fullName, String otp) throws EmailServiceException;

    void sendPasswordResetOtp(String recipient, String fullName, String otp) throws EmailServiceException;
}
