package com.monetique.PinSenderV0.Services;

import com.monetique.PinSenderV0.Interfaces.IOtpService;

import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService implements IOtpService {
    @Autowired
    private SmsService smsService;
    @Autowired
    private HSMService hsmService;
   @Autowired
   private IStatisticservices statisticservices;


    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);


    // A simple in-memory store for OTPs (
    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, LocalDateTime> otpExpiryStore = new HashMap<>();

    private static final int OTP_VALIDITY_MINUTES = 1; // OTP validity (e.g., 1 minutes)

    @Override
    public String sendOtp(String phoneNumber) {
        // Generate a 6-digit OTP
        String otp = generateOtp();
        logger.info("Generate a 6-digit OTP ");
        // Store the OTP against the phone number
        otpStore.put(phoneNumber, otp);
        otpExpiryStore.put(phoneNumber, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));

        String message = String.format("Votre code de verification est : %s. Ce code est temporaire et strictement confidentiel.", otp);
        
        smsService.sendSms(phoneNumber, message)
                .doOnSuccess(response -> System.out.println("SMS sent successfully: " + response))
                .doOnError(error -> System.err.println("Error sending OTP SMS: " + error.getMessage()))
                .subscribe(); // Non-blocking
        logger.info("sending OTP SMS"+otp);
        return otp;
    }



    @Override
    public boolean validateOtp(OtpValidationRequest otpValidationRequest) {
        // Check if the OTP matches the one we sent
        String phoneNumber =otpValidationRequest.getPhoneNumber();
        String otp =otpValidationRequest.getOtp();
        String cartnumber= otpValidationRequest.getCardNumber();

        System.out.println("cardnummber " + cartnumber );
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        otpValidationRequest.setAgentId(currentUser.getId());
        otpValidationRequest.setBranchId(currentUser.getAgency() != null ? currentUser.getAgency().getId() : null);
        otpValidationRequest.setBankId(currentUser.getBank() != null ? currentUser.getBank().getId() : null);
        if (isOtpExpired(phoneNumber)) {
            System.out.println("OTP for phone number " + phoneNumber + " has expired.");
            return false;
        }

        String storedOtp = otpStore.get(phoneNumber);
        if (storedOtp != null && storedOtp.equals(otp)) {
            System.out.println("OTP validated successfully for phone number: " + phoneNumber);
            // 2. Calculer le PIN en clair
            String clearPin = hsmService.clearpin(cartnumber);
            // Envoyer le PIN au téléphone

            String message = String.format("Votre code PIN est : %s. Ce code est strictement personnel et confidentiel. Ne le partagez jamais et ne l'ecrivez pas.", clearPin);
            smsService.sendSms(phoneNumber, message)
                    .doOnSuccess(response -> System.out.println("SMS sent successfully: " + response))
                    .doOnError(error -> System.err.println("Error sending OTP SMS: " + error.getMessage()))
                    .subscribe(); // Non-blocking
            statisticservices.logSentItem(otpValidationRequest.getAgentId(), otpValidationRequest.getBranchId(), otpValidationRequest.getBankId(), "PIN");

            return true;
        } else {
            System.out.println("Invalid OTP for phone number: " + phoneNumber);
            return false;
        }
    }

    @Override
    public String resendOtp(String phoneNumber) {
        // Resend OTP by generating a new one and resetting the expiration time
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        Long AgentId= currentUser.getId();
        Long BranchId= currentUser.getAgency() != null ? currentUser.getAgency().getId() : null;
        Long BankId= currentUser.getBank() != null ? currentUser.getBank().getId() : null;
        String newOtp = sendOtp(phoneNumber);
        System.out.println("Resent OTP to phone number: " + phoneNumber);
        statisticservices.logSentItem(AgentId, BranchId, BankId, "OTP");

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
