package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Interfaces.IOtpService;

import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service

public class OtpService implements IOtpService {
    @Autowired
    SmsService smsService;
    @Autowired
    HSMService hsmService;


    // A simple in-memory store for OTPs (for demonstration)
    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, LocalDateTime> otpExpiryStore = new HashMap<>();

    private static final int OTP_VALIDITY_MINUTES = 1; // OTP validity (e.g., 5 minutes)

    @Override
    public String sendOtp(String phoneNumber) {
        // Generate a 6-digit OTP
        String otp = generateOtp();
        // Store the OTP against the phone number
        otpStore.put(phoneNumber, otp);
        otpExpiryStore.put(phoneNumber, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        String message = "Your OTP is " + otp;
        smsService.sendSms(phoneNumber, message)
                .doOnSuccess(response -> System.out.println("SMS sent successfully: " + response))
                .doOnError(error -> System.err.println("Error sending OTP SMS: " + error.getMessage()))
                .subscribe(); // Non-blocking

        return otp;
    }



    @Override
    public boolean validateOtp(OtpValidationRequest otpValidationRequest) {
        // Check if the OTP matches the one we sent
        String phoneNumber =otpValidationRequest.getPhoneNumber();
        String otp =otpValidationRequest.getOtp();
        String cardNumber =otpValidationRequest.getCardNumber();

        if (isOtpExpired(phoneNumber)) {
            System.out.println("OTP for phone number " + phoneNumber + " has expired.");
            return false;
        }

        String storedOtp = otpStore.get(phoneNumber);
        if (storedOtp != null && storedOtp.equals(otp)) {
            System.out.println("OTP validated successfully for phone number: " + phoneNumber);
            String pin = hsmService.generatePin(cardNumber);
            // Envoyer le PIN au téléphone
            smsService.sendSms(phoneNumber, "Votre PIN est : " + pin);
            return true;
        } else {
            System.out.println("Invalid OTP for phone number: " + phoneNumber);
            return false;
        }
    }

    @Override
    public String resendOtp(String phoneNumber) {
        // Resend OTP by generating a new one and resetting the expiration time
        String newOtp = sendOtp(phoneNumber);
        System.out.println("Resent OTP to phone number: " + phoneNumber);
        return newOtp;

    }

    @Override
    public boolean isOtpExpired(String phoneNumber) {
        LocalDateTime expirationTime = otpExpiryStore.get(phoneNumber);
        if (expirationTime == null || LocalDateTime.now().isAfter(expirationTime)) {
            return true;
        }
        return false;
    }

    // Generate a 6-digit OTP
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
