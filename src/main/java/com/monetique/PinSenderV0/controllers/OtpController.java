package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.OtpResendResult;
import com.monetique.PinSenderV0.payload.response.OtpValidationResult;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> resendOtp(@RequestBody String phoneNumber) {
        try {
            OtpResendResult result = otpService.resendOtp(phoneNumber);

            switch (result.getStatus()) {
                case SUCCESS:
                    return ResponseEntity.ok(new MessageResponse("OTP resent successfully.", 200));

                case NO_EXISTING_OTP:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("No OTP exists for this number. Please restart the verification process.", 400));

                case TOO_MANY_ATTEMPTS:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Too many OTP resend attempts. This number is temporarily blocked.", 403));

                case RATE_LIMIT_EXCEEDED:
                    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                            .body(new MessageResponse("Too many resend requests. Please wait before trying again.", 429));

                case NUMBER_BLOCKED:
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("This number is temporarily blocked. Please try again later.", 403));

                case ERROR:
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("An unexpected error occurred while resending OTP.", 500));
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("An unexpected error occurred.", 500));
        }
    }

}
