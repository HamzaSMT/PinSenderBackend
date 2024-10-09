package com.monetique.PinSenderV0.Interfaces;

public interface IOtpService {
    // Method to send OTP to the provided phone number
    String sendOtp(String phoneNumber);

    // Method to validate the OTP input by the user
    boolean validateOtp(String phoneNumber, String otp);
    // Method to resend OTP
    String resendOtp(String phoneNumber);

    // Method to check if OTP is expired
    boolean isOtpExpired(String phoneNumber);
}
