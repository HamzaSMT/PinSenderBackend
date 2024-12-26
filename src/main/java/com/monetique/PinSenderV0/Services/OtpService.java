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
        // Resend OTP by generating a new one and resetting the expiration time
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        logger.info("Resent OTP to phone number: " + phoneNumber);
        String otp = generateOtp();
        otpStore.put(phoneNumber, otp);
        otpExpiryStore.put(phoneNumber, LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        String message = String.format("Votre code de verification est : %s. Ce code est temporaire.", otp);
        // Use a blocking call here to resolve the Mono synchronously
        try {
            String response = smsService.sendSms(phoneNumber, message)
                    .doOnSuccess(res -> {
                        logger.info("SMS sent successfully: {}", res);
                        // Only log to statistic services on success

                        statisticservices.logSentItem(currentUser.getId(),
                                currentUser.getAgency() != null ? currentUser.getAgency().getId() : null,
                                currentUser.getBank() != null ? currentUser.getBank().getId() : null,
                                "OTP");
                    })
                    .doOnError(error -> logger.error("Error sending OTP SMS: {}", error.getMessage()))
                    .block(); // Block to get the result synchronously

            logger.info("OTP successfully sent: {}", otp);
            return response; // Return the OTP upon success
        } catch (Exception e) {
            logger.error("Fallback: Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send OTP SMS.", e);
        }

    }

    @Override
    public boolean validateOtp(OtpValidationRequest otpValidationRequest ) {
        // Check if the OTP matches the one we sent
        String phoneNumber =otpValidationRequest.getPhoneNumber();
        String otp =otpValidationRequest.getOtp();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();

        if (isOtpExpired(phoneNumber)) {
            System.out.println("OTP for phone number " + phoneNumber + " has expired.");
            return false;
        }
        String storedOtp = otpStore.get(phoneNumber);
        String cartNumber= otpValidationRequest.getCardNumber();
        if (storedOtp != null && storedOtp.equals(otp)) {
            logger.info("OTP validated successfully for phone number: " + phoneNumber);
            // 2. Calculate the clear PIN using HSM service
            String clearPin = hsmService.clearpin(cartNumber);
            // 3. Send the PIN to the phone number via SMS
            String message = String.format("Votre code PIN est : %s. Ce code est strictement personnel et confidentiel." +
                    " Ne le partagez jamais et ne l'écrivez pas.", clearPin);
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
            return true;
        } else {
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
}
