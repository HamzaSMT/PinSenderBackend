package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private IOtpService otpService;



    // Endpoint to validate OTP
    @PostMapping("/validate")
    public ResponseEntity<String> validateOtp(@RequestBody OtpValidationRequest request) {
        boolean isValid = otpService.validateOtp(request.getPhoneNumber(), request.getOtp());

        if (isValid) {
            return ResponseEntity.ok("Phone number validated successfully.");
        } else {
            return ResponseEntity.status(400).body("Invalid OTP.");
        }
    }
    @PostMapping("/resend")
    public ResponseEntity<String> resendOtp(@RequestBody String gsmnumber) {
        String otp = otpService.resendOtp(gsmnumber);
        return ResponseEntity.ok("OTP resent to " + gsmnumber);
    }

}
