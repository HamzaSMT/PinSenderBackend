package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.HttpMethodEnum;
import com.monetique.PinSenderV0.models.UserSession;
import com.monetique.PinSenderV0.payload.request.CardholderVerificationRequest;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.UserSessionRepository;
import com.monetique.PinSenderV0.security.services.CardholderService;
import com.monetique.PinSenderV0.security.services.MonitoringService;
import com.monetique.PinSenderV0.security.services.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Corrected import to Jakarta
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    private CardholderService cardholderService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private MonitoringService monitoringService;  // Injected monitoring service to log events

    // Requires JWT authentication
    @PostMapping("/verifyCardholder")
    public ResponseEntity<?> verifyCardholder(@RequestBody CardholderVerificationRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String roles = authentication.getAuthorities().toString();  // Get user roles
        LocalDateTime requestTime = LocalDateTime.now();  // Track request time

        logger.info("Received cardholder verification request for cardNumber: {} from user: {} with roles: {}",
                request.getCardNumber(), username, roles);

        // Fetch the active session for the current user
        UserSession currentSession = userSessionRepository.findCurrentSessionByUsername(username);

        try {
            boolean isValid = cardholderService.verifyCardholder(
                    request.getCardNumber(),
                    request.getCin(),
                    request.getPhoneNumber(),
                    request.getExpirationDate()
            );

            if (isValid) {
                String otp = otpService.generateOtp(request.getPhoneNumber());
                otpService.sendOtp(request.getPhoneNumber(), otp);
                logger.info("OTP sent successfully to phoneNumber: {} by user: {}", request.getPhoneNumber(), username);

                // Log the successful request to monitoring with UserSession
                monitoringService.logRequest(currentSession, "/verifyCardholder", HttpMethodEnum.POST, 200,
                        Duration.between(requestTime, LocalDateTime.now()).toMillis());

                return ResponseEntity.ok(new MessageResponse("OTP sent successfully!", HttpStatus.OK.value()));
            } else {
                logger.warn("Invalid cardholder information for cardNumber: {} by user: {}", request.getCardNumber(), username);

                // Log the failed request to monitoring
                monitoringService.logRequest(currentSession, "/verifyCardholder", HttpMethodEnum.POST, 404,
                        Duration.between(requestTime, LocalDateTime.now()).toMillis());

                throw new ResourceNotFoundException("Cardholder", "cardNumber", request.getCardNumber());
            }
        } catch (Exception e) {
            logger.error("Error during cardholder verification for cardNumber: {} by user: {}", request.getCardNumber(), username, e);

            // Log the error to monitoring
            monitoringService.logRequest(currentSession, "/verifyCardholder", HttpMethodEnum.POST, 500,
                    Duration.between(requestTime, LocalDateTime.now()).toMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error during verification", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // Requires JWT authentication
    @PostMapping("/validateOtp")
    public ResponseEntity<?> validateOtp(@RequestBody OtpValidationRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String roles = authentication.getAuthorities().toString();  // Get user roles
        LocalDateTime requestTime = LocalDateTime.now();  // Track request time

        logger.info("Received OTP validation request for phoneNumber: {} from user: {} with roles: {}",
                request.getPhoneNumber(), username, roles);

        // Fetch the active session for the current user
        UserSession currentSession = userSessionRepository.findCurrentSessionByUsername(username);

        try {
            boolean isValidOtp = otpService.validateOtp(request.getPhoneNumber(), request.getOtp());

            if (isValidOtp) {
                cardholderService.sendPin(request.getPhoneNumber());
                logger.info("PIN sent successfully to phoneNumber: {} by user: {}", request.getPhoneNumber(), username);

                // Log the successful request to monitoring
                monitoringService.logRequest(currentSession, "/validateOtp", HttpMethodEnum.POST, 200,
                        Duration.between(requestTime, LocalDateTime.now()).toMillis());

                return ResponseEntity.ok(new MessageResponse("OTP validated successfully, PIN sent!", HttpStatus.OK.value()));
            } else {
                logger.warn("Invalid OTP for phoneNumber: {} by user: {}", request.getPhoneNumber(), username);

                // Log the failed request to monitoring
                monitoringService.logRequest(currentSession, "/validateOtp", HttpMethodEnum.POST, 400,
                        Duration.between(requestTime, LocalDateTime.now()).toMillis());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Invalid OTP!", HttpStatus.BAD_REQUEST.value()));
            }
        } catch (Exception e) {
            logger.error("Error during OTP validation for phoneNumber: {} by user: {}", request.getPhoneNumber(), username, e);

            // Log the error to monitoring
            monitoringService.logRequest(currentSession, "/validateOtp", HttpMethodEnum.POST, 500,
                    Duration.between(requestTime, LocalDateTime.now()).toMillis());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error during OTP validation", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

}
