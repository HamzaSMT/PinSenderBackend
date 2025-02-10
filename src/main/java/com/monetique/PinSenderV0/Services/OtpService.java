package com.monetique.PinSenderV0.Services;
import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.controllers.WebSocketController;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.SMSResponse;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService implements IOtpService {
    @Autowired
    private SmsService smsService;
    @Autowired
    private HSMService hsmService;
    @Autowired
    private IStatisticservices statisticservices;
    @Autowired
    private HashingService hashingService;



    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);


    // A simple in-memory store for OTPs (
    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, LocalDateTime> otpExpiryStore = new HashMap<>();
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MINUTES = 3;
    private final Map<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> blockedNumbers = new ConcurrentHashMap<>();
    private static final int MAX_RESEND_ATTEMPTS = 3;
    private static final Duration RESEND_INTERVAL = Duration.ofMinutes(1);
    private final Map<String, Integer> otpResendAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastResendTime = new ConcurrentHashMap<>();

    private static final int OTP_VALIDITY_MINUTES = 1; // OTP validity (e.g., 1 minutes)

    @Override
    public SMSResponse sendOtp(VerifyCardholderRequest request) {
        String otp = generateOtp();
        logger.info("Generated a 6-digit OTP: {}", otp);

        otpStore.put(request.getGsm(), otp);
        otpExpiryStore.put(request.getGsm(), LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        String message = String.format("Votre code de verification est : %s. Ce code est temporaire.", otp);

        try {
            String smsResult = smsService.sendSms(request.getGsm(), message)
                    .block(); // Blocking call for synchronous execution

            if ("SMS sending failed.".equals(smsResult)) {
                // SMS service returned fallback message
                logger.error("SMS service failed to send OTP.");
                return new SMSResponse("Failure", "Failed to send OTP SMS.", null, 500);
            }

            logger.info("SMS sent successfully to {}: {}", request.getGsm(), smsResult);
            statisticservices.logSentItem(request.getAgentId(), request.getBranchId(), request.getBankId(), "OTP");
            return new SMSResponse("Success", "OTP sent successfully.", otp, 200);

        } catch (Exception e) {
            logger.error("Unexpected error occurred while sending OTP to {}: {}", request.getGsm(), e.getMessage());
            return new SMSResponse("Failure", "Failed to send OTP SMS due to an unexpected error.", null, 500);
        }

    }


    @Override
    public String resendOtp(String phoneNumber) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        logger.info("Attempting to resend OTP to phone number: {}", phoneNumber);

        // Vérifier si un OTP existe déjà pour ce numéro
        String otp = otpStore.get(phoneNumber);
        if (otp == null) {
            throw new IllegalStateException("Aucun OTP à renvoyer pour ce numéro.");
        }

        // Vérifier le rate limit
        LocalDateTime lastSentTime = lastResendTime.get(phoneNumber);
        if (lastSentTime != null && Duration.between(lastSentTime, LocalDateTime.now()).compareTo(RESEND_INTERVAL) < 0) {
            throw new IllegalStateException("Trop de demandes de renvoi d’OTP. Veuillez patienter.");
        }

        int resendAttempts = otpResendAttempts.getOrDefault(phoneNumber, 0);
        if (resendAttempts >= MAX_RESEND_ATTEMPTS) {
            throw new IllegalStateException("Limite de renvoi d’OTP atteinte pour ce numéro.");
        }

        // Mettre à jour le compteur de tentatives et le timestamp du dernier envoi
        otpResendAttempts.put(phoneNumber, resendAttempts + 1);
        lastResendTime.put(phoneNumber, LocalDateTime.now());

        String message = String.format("Votre code de verification est : %s. Ce code est temporaire.", otp);

        try {
            String response = smsService.sendSms(phoneNumber, message)
                    .doOnSuccess(res -> {
                        logger.info("SMS sent successfully: {}", res);

                        statisticservices.logSentItem(currentUser.getId(),
                                currentUser.getAgency() != null ? currentUser.getAgency().getId() : null,
                                currentUser.getBank() != null ? currentUser.getBank().getId() : null,
                                "OTP");
                    })
                    .doOnError(error -> logger.error("Error sending OTP SMS: {}", error.getMessage()))
                    .block();

            logger.info("OTP successfully resent: {}", otp);
            return response;
        } catch (Exception e) {
            logger.error("Fallback: Failed to resend OTP to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Échec du renvoi de l’OTP.", e);
        }
    }


    @Override
    public boolean validateOtp(OtpValidationRequest otpValidationRequest )throws Exception {
        // Check if the OTP matches the one we sent
        String phoneNumber =otpValidationRequest.getPhoneNumber();
        String otp =otpValidationRequest.getOtp();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        if (isBlocked(phoneNumber)) {
            logger.warn("Tentative de validation d'OTP pour un numéro bloqué : {}", phoneNumber);
            return false;
        }
        if (isOtpExpired(phoneNumber)) {
            System.out.println("OTP for phone number " + phoneNumber + " has expired.");
            return false;
        }
        String storedOtp = otpStore.get(phoneNumber);
        String cartNumber= otpValidationRequest.getCardNumber();
        String cardHash = hashingService.hashPAN(cartNumber);
        if (storedOtp != null && storedOtp.equals(otp)) {
            logger.info("OTP validated successfully for phone number: " + phoneNumber);
            // 2. Calculate the clear PIN using HSM service
            String clearPin = hsmService.clearpin(cartNumber,cardHash);
            // 3. Send the PIN to the phone number via SMS
            String message = String.format("Votre code PIN est : [ %s ]. Ce code est strictement personnel et confidentiel." +
                    " Ne le partagez jamais.", clearPin);
            try {
                smsService.sendSms(phoneNumber, message)
                        .doOnSuccess(response -> {
                            // Log the success response after sending the SMS
                            logger.info("SMS sent successfully: {}", response);

                            // Log the statistic item after SMS success
                            statisticservices.logSentItem(
                                    currentUser.getId(),
                                    currentUser.getAgency() != null ? currentUser.getAgency().getId() : null,
                                    currentUser.getBank() != null ? currentUser.getBank().getId() : null,
                                    "PIN");
                        })
                        .doOnError(error -> {
                            // Handle errors during SMS sending
                            logger.error("Error sending PIN SMS: {}: {}", error.getMessage());
                        })
                        .block(); // Block to ensure SMS is sent before continuing
            } catch (Exception e) {
                logger.error("Error during SMS sending or statistic logging: {}", e.getMessage());
            }
            otpAttempts.remove(phoneNumber);
            return true;
        } else {
            logger.error("Invalid OTP for phone number: {}", phoneNumber);
            otpAttempts.put(phoneNumber, otpAttempts.getOrDefault(phoneNumber, 0) + 1);

            if (otpAttempts.get(phoneNumber) >= MAX_OTP_ATTEMPTS) {
                blockedNumbers.put(phoneNumber, LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES));
                logger.warn("Numéro {} bloqué pour {} minutes après {} tentatives échouées.",
                        phoneNumber, BLOCK_DURATION_MINUTES, MAX_OTP_ATTEMPTS);
            }

            logger.error("OTP invalide pour {}. Tentatives restantes : {}",
                    phoneNumber, MAX_OTP_ATTEMPTS - otpAttempts.get(phoneNumber));
            logger.error("Invalid OTP for phone number: {}", phoneNumber);
            return false;
        }
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


    private boolean isBlocked(String phoneNumber) {
        if (blockedNumbers.containsKey(phoneNumber)) {
            LocalDateTime unblockTime = blockedNumbers.get(phoneNumber);
            if (LocalDateTime.now().isBefore(unblockTime)) {
                return true; // Toujours bloqué
            } else {
                blockedNumbers.remove(phoneNumber); // Débloquer après la durée
                otpAttempts.remove(phoneNumber); // Réinitialiser les tentatives
            }
        }
        return false;
    }

}
