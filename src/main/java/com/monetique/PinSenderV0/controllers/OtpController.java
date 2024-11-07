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




    // Endpoint to validate OTP
    @PostMapping("/validate")
    public ResponseEntity<MessageResponse> validateOtp(@RequestBody OtpValidationRequest request) {
        boolean isValid = otpService.validateOtp(request);

        if (isValid) {


            return ResponseEntity.ok(new MessageResponse("Phone number validated successfully.",200));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid OTP",400));
        }
    }
    @PostMapping("/resend")
    public ResponseEntity<String> resendOtp(@RequestBody String gsmnumber) {
        String otp = otpService.resendOtp(gsmnumber);
        return ResponseEntity.ok("OTP "+ otp +"resent to " + gsmnumber);
    }

}
