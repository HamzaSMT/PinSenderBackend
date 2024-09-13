package com.monetique.PinSenderV0.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private Map<String, String> otpStorage = new HashMap<>();

    @Autowired
    private InfobipSmsService infobipSmsService;

    public String generateOtp(String phoneNumber) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(phoneNumber, otp);
        return otp;
    }

    public void sendOtp(String phoneNumber, String otp) {
        try {
            infobipSmsService.sendSms(phoneNumber, "Your OTP is: " + otp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        return otp.equals(otpStorage.get(phoneNumber));
    }
}
