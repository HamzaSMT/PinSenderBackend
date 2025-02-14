package com.monetique.PinSenderV0.Services;
import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.OtpResendResult;
import com.monetique.PinSenderV0.payload.response.OtpValidationResult;
import com.monetique.PinSenderV0.payload.response.OtpValidationStatus;
import com.monetique.PinSenderV0.payload.response.SMSResponse;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
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


    // Maps for OTPs, attempts, expiry times, blocked numbers, resend attempts, and timestamps
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> otpExpiryStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> otpAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> blockedNumbers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> otpResendAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lastResendTime = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> cardToPhoneMap = new ConcurrentHashMap<>();

    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_MINUTES = 1;
    private static final int MAX_RESEND_ATTEMPTS = 5;
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(30);
    private static final int OTP_VALIDITY_MINUTES = 3;

    @Override
    public SMSResponse sendOtp(VerifyCardholderRequest request) {
        String otp = generateOtp();
        logger.info("Generated a 6-digit OTP: {}", otp);

        otpStore.put(request.getGsm(), otp);
        otpExpiryStore.put(request.getGsm(), LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        // Associer la carte bancaire au numéro de téléphone
        cardToPhoneMap.put(request.getCardNumber(), request.getGsm());
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

/*
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

*/
    @Override
    public OtpValidationResult validateOtp(OtpValidationRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String otp = request.getOtp();
        String cardNumber = request.getCardNumber();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        // Vérifier si le numéro est bloqué
        if (isBlocked(phoneNumber)) {
            logger.warn("🚨 [BLOQUÉ] Numéro {}. Impossible de valider l'OTP.", phoneNumber);
            return new OtpValidationResult(OtpValidationStatus.NUMBER_BLOCKED);
        }
        if (!isPhoneLinkedToCard(cardNumber, phoneNumber)) {
            logger.warn("❌ [ÉCHEC] Le numéro {} n'est pas associé à la carte {}.", phoneNumber, cardNumber);
            return new OtpValidationResult(OtpValidationStatus.INVALID_PHONE);
        }

        // Vérifier si l'OTP est expiré
        if (isOtpExpired(phoneNumber)) {
            logger.warn("❌ [EXPIRÉ] OTP expiré pour {}. Tentatives actuelles : {}/{}",
                    phoneNumber, otpAttempts.getOrDefault(phoneNumber, 0), MAX_OTP_ATTEMPTS);
            // Ne pas réinitialiser les tentatives si l'OTP a expiré
            return new OtpValidationResult(OtpValidationStatus.OTP_EXPIRED);
        }

        // Vérifier si l'OTP est correct
        String storedOtp = otpStore.get(phoneNumber);
        if (storedOtp != null && storedOtp.equals(otp)) {
            return processSuccessfulOtpValidation(phoneNumber, cardNumber, currentUser);
        } else {
            return processFailedOtpAttempt(phoneNumber);
        }
    }

    private OtpValidationResult processSuccessfulOtpValidation(String phoneNumber, String cardNumber, UserDetailsImpl currentUser) {
        logger.info("✅ [SUCCÈS] OTP validé pour {}. Réinitialisation du compteur.", phoneNumber);

        try {
            String cardHash = hashingService.hashPAN(cardNumber);
            String clearPin = hsmService.clearpin(cardNumber, cardHash);

            String message = String.format("Votre code PIN est : [ %s ]." +
                    " Ce code est strictement personnel et confidentiel. Ne le partagez jamais.", clearPin);

            smsService.sendSms(phoneNumber, message)
                    .doOnSuccess(response -> {
                        logger.info("📩 [SMS ENVOYÉ] : {}", response);
                        statisticservices.logSentItem(
                                currentUser.getId(),
                                currentUser.getAgency() != null ? currentUser.getAgency().getId() : null,
                                currentUser.getBank() != null ? currentUser.getBank().getId() : null,
                                "PIN"
                        );
                        resetOtpAttempts(phoneNumber);
                    })
                    .doOnError(error -> logger.error("❌ [ERREUR SMS] : {}", error.getMessage()))
                    .block();


            return new OtpValidationResult(OtpValidationStatus.SUCCESS);
        } catch (Exception e) {
            logger.error("❌ [ERREUR] lors de l'envoi du SMS ou de la journalisation : {}", e.getMessage());
            return new OtpValidationResult(OtpValidationStatus.ERROR);
        }
    }

    private OtpValidationResult processFailedOtpAttempt(String phoneNumber) {
        int currentAttempts = otpAttempts.getOrDefault(phoneNumber, 0) + 1;

        if (currentAttempts >= MAX_OTP_ATTEMPTS) {
            blockNumber(phoneNumber);
            logger.warn("🚨 [BLOQUÉ] {} après {} tentatives échouées.", phoneNumber, MAX_OTP_ATTEMPTS);
            return new OtpValidationResult(OtpValidationStatus.NUMBER_BLOCKED);
        }

        // Incrémente le compteur de tentatives
        otpAttempts.put(phoneNumber, currentAttempts);
        logger.warn("❌ [INVALIDE] OTP incorrect pour {}. Tentatives : {}/{}", phoneNumber, currentAttempts, MAX_OTP_ATTEMPTS);

        return new OtpValidationResult(OtpValidationStatus.INVALID_OTP);
    }


    private void resetOtpAttempts(String phoneNumber) {
        otpAttempts.remove(phoneNumber);
        otpStore.remove(phoneNumber);
        otpExpiryStore.remove(phoneNumber);
        logger.info("🔄 [RÉINITIALISATION] Tentatives et OTP supprimés pour {}", phoneNumber);
    }

    private void blockNumber(String phoneNumber) {
        blockedNumbers.put(phoneNumber, LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES));
        logger.warn("🚫 [BLOQUÉ] Numéro {} bloqué pour {} minutes après {} tentatives échouées.", phoneNumber, BLOCK_DURATION_MINUTES, MAX_OTP_ATTEMPTS);
    }

    private boolean isOtpExpired(String phoneNumber) {
        LocalDateTime expirationTime = otpExpiryStore.get(phoneNumber);
        return expirationTime == null || LocalDateTime.now().isAfter(expirationTime);
    }
    private boolean isPhoneLinkedToCard(String cardNumber, String phoneNumber) {
        return cardToPhoneMap.containsKey(cardNumber) && cardToPhoneMap.get(cardNumber).contains(phoneNumber);
    }

    private boolean isBlocked(String phoneNumber) {
        LocalDateTime unblockTime = blockedNumbers.get(phoneNumber);
        if (unblockTime != null) {
            if (LocalDateTime.now().isBefore(unblockTime)) {
                logger.warn("🔒 [BLOQUÉ] Le numéro {} est bloqué jusqu'à {}", phoneNumber, unblockTime);
                return true;
            } else {
                logger.info("🟢 [DÉBLOQUÉ] Le numéro {} a dépassé la durée de blocage. Déblocage en cours...", phoneNumber);
                blockedNumbers.remove(phoneNumber);
                otpAttempts.remove(phoneNumber);
            }
        }
        return false;
    }


    // Generate a 6-digit OTP
    private String generateOtp() {
        return String.format("%06d", 100000 + new Random().nextInt(900000));
    }


    @Override
    public OtpResendResult resendOtp(String phoneNumber) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        logger.info("Attempting to resend OTP to phone number: {}", phoneNumber);
        String otpStored = otpStore.get(phoneNumber);
        if (otpStored == null) {
            System.out.println("❌ No OTP found for " + phoneNumber);
        } else {
            System.out.println("✅ OTP Retrieved: " + otpStored);
        }
        // Check if the number is temporarily blocked

        if (isBlocked(phoneNumber)) {
            logger.warn("🚨 [BLOQUÉ] Numéro {}. Impossible de valider l'OTP.", phoneNumber);
            return new OtpResendResult(OtpResendResult.Status.NUMBER_BLOCKED, "This number is temporarily blocked.");
        }

        // Check if an OTP exists for this phone number
        if (!otpStore.containsKey(phoneNumber)) {
            logger.warn("No existing OTP found for phone number: {}", phoneNumber);
            return new OtpResendResult(OtpResendResult.Status.NO_EXISTING_OTP, "No OTP exists for this number.");
        }

        // Check if resend interval has passed
        LocalDateTime lastSentTime = lastResendTime.get(phoneNumber);
        if (lastSentTime != null && Duration.between(lastSentTime, LocalDateTime.now()).compareTo(RESEND_INTERVAL) < 0) {
            logger.warn("Resend interval not met for phone number: {}. Last sent time: {}", phoneNumber, lastSentTime);
            return new OtpResendResult(OtpResendResult.Status.RATE_LIMIT_EXCEEDED, "Too many resend requests.");
        }

        // Check if the maximum resend attempts have been reached
        int resendAttempts = otpResendAttempts.getOrDefault(phoneNumber, 0);
        if (resendAttempts >= MAX_RESEND_ATTEMPTS) {
            blockedNumbers.put(phoneNumber, LocalDateTime.now());
            logger.warn("Too many resend attempts for phone number: {}. Blocking further attempts.", phoneNumber);
            return new OtpResendResult(OtpResendResult.Status.TOO_MANY_ATTEMPTS, "Too many resend attempts.");
        }

        // Delete old OTP and expiry information
        logger.info("Deleting old OTP for phone number: {}", phoneNumber);
        otpStore.remove(phoneNumber);
        otpExpiryStore.remove(phoneNumber);

        // Generate and store a new OTP
        String newOtp = generateOtp();
        logger.info("Generated new OTP: {} for phone number: {}", newOtp, phoneNumber);
        otpStore.put(phoneNumber, newOtp);
        otpExpiryStore.put(phoneNumber, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));

        // Increment resend attempts and update last sent time
        otpResendAttempts.put(phoneNumber, resendAttempts + 1);
        lastResendTime.put(phoneNumber, LocalDateTime.now());

        // Send OTP via SMS
        try {
            String message = String.format("Votre code de verification est : %s. Ce code est temporaire.", newOtp);
            logger.info("Sending OTP message to phone number: {}. Message: {}", phoneNumber, message);

            smsService.sendSms(phoneNumber, message)
                    .doOnSuccess(response -> {
                        logger.info("📩 [SMS ENVOYÉ] : {}", response);
                        statisticservices.logSentItem(
                                currentUser.getId(),
                                currentUser.getAgency() != null ? currentUser.getAgency().getId() : null,
                                currentUser.getBank() != null ? currentUser.getBank().getId() : null,
                                "OTP"
                        );
                    })
                    .doOnError(error -> logger.error("❌ [ERREUR SMS] : {}", error.getMessage()))
                    .block();

            logger.info("OTP resent successfully to phone number: {}", phoneNumber);
            return new OtpResendResult(OtpResendResult.Status.SUCCESS, "OTP resent successfully.");
        } catch (Exception e) {
            logger.error("❌ [ERROR] Failed to resend OTP for phone number: {}. Exception: {}", phoneNumber, e.getMessage());
            return new OtpResendResult(OtpResendResult.Status.ERROR, "Failed to resend OTP.");
        }
    }

    @Scheduled(fixedRate = 900000) // Exécution toutes les 15 minutes
    public void cleanUpExpiredOtp() {
        LocalDateTime now = LocalDateTime.now();

        // Remove expired OTPs from otpExpiryStore
        otpExpiryStore.entrySet().removeIf(entry -> entry.getValue() != null && entry.getValue().isBefore(now));

        // Remove expired OTPs from otpStore, ensuring otpExpiryStore.get() is not null
        otpStore.entrySet().removeIf(entry -> {
            LocalDateTime expiryTime = otpExpiryStore.get(entry.getKey());
            return expiryTime != null && expiryTime.isBefore(now);
        });
    }

    @Scheduled(fixedRate = 3600000) // Exécution toutes les 1 heure
    public void unblockNumbers() {
        LocalDateTime now = LocalDateTime.now();
        blockedNumbers.entrySet().removeIf(entry -> entry.getValue() != null && entry.getValue().isBefore(now.minusMinutes(BLOCK_DURATION_MINUTES)));
    }

}


