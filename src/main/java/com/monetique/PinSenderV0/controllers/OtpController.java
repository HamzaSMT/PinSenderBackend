package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.OtpValidationResult;
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
            OtpValidationResult result = otpService.validateOtp(request);

            switch (result.getStatus()) {
                case SUCCESS:
                    return ResponseEntity.ok(new MessageResponse("Phone number validated successfully.", 200));

                case INVALID_OTP:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Invalid OTP", 400));

                case OTP_EXPIRED:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("OTP expired. Please request a new one.", 400));

                case NUMBER_BLOCKED:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Too many failed attempts. Number is temporarily blocked.", 403));

                case ERROR:
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("An unexpected error occurred.", 500));
            }
        } catch (Exception ex) {
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
