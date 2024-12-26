package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private IOtpService otpService;


    @PostMapping("/validate")
    public ResponseEntity<MessageResponse> validateOtp(@RequestBody OtpValidationRequest request) {
        try {
            boolean isValid = otpService.validateOtp(request);
            if (isValid) {
                return ResponseEntity.ok(new MessageResponse("Phone number validated successfully.", 200));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Invalid OTP", 400));
            }
        } catch (Exception ex) {
            // Log and handle generic exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An unexpected error occurred.", 500));
        }
    }



    @PostMapping("/resend")
    public ResponseEntity<MessageResponse> resendOtp(@RequestBody String gsmNumber) {
        try {
            String otp = otpService.resendOtp(gsmNumber);
            return ResponseEntity.ok(new MessageResponse("Code OTP resent successfully.", 200));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Failed to resend OTP", 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An unexpected error occurred", 500));
        }
    }
}
