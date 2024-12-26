package com.monetique.PinSenderV0.Services.Cardholder;
import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.Services.HashingService;
import com.monetique.PinSenderV0.Services.managementbank.AgencyService;
import com.monetique.PinSenderV0.controllers.WebSocketController;
import com.monetique.PinSenderV0.models.Card.TabCardHolder;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.SMSResponse;
import com.monetique.PinSenderV0.repository.TabCardHolderRepository;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardholderConsumer {


    private static final Logger logger = LoggerFactory.getLogger(AgencyService.class);
    @Autowired
    private TabCardHolderRepository cardholderRepository;
    @Autowired
    private IOtpService otpService;
    @Autowired
    private IStatisticservices statisticservices;
    @Autowired
    private WebSocketController webSocketController;
    @Autowired
    private HashingService hashingService;

    @RabbitListener(queues = "cardholder.queue")

    public void handleMessage(VerifyCardholderRequest request) throws Exception {
        // Simulate processing the verification

        String authenticatedUserBankCode = request.getAuthenticatedUserBankCode();
        String cardHash = hashingService.hashPAN(request.getCardNumber());
        logger.info("Received verification request for cardholder "+request.getNationalId());
        // Check if the cardholder belongs to the same bank
        boolean isSameBank = isCardholderAndUserInSameBank(cardHash, authenticatedUserBankCode);
        if (!isSameBank) {
            logger.warn("Unauthorized access attempt: Cardholder and user are in different banks or cardholder not found.");
            webSocketController.notifyClient(request.getCardNumber(),
                    "Unauthorized access: Cardholder belongs to a different bank or cardholder not found.", 404);
            return;
            // Stop further processing if bank check fails or cardholder not found
        }

        boolean valid= checkIfExists(cardHash,
                request.getFinalDate(),
                request.getNationalId(),
                request.getGsm());
        if (valid) {
            logger.info("Cardholder verified successfully for GSM: " + request.getGsm());
            // Proceed with OTP generation, sending SMS, etc.
            SMSResponse smsResponse = otpService.sendOtp(request); // Get response from sendOtp
            logger.info("OTP will be sent to phone number: " + request.getGsm());
            // Check the response status and handle accordingly
            if ("Success".equals(smsResponse.getStatus())) {
                // OTP sent successfully
                webSocketController.notifyClient(request.getCardNumber(),
                        "OTP sent successfully to Cardholder phone",
                        200);
            } else {
                // Failed to send OTP
                logger.error("Failed to send OTP to {}: {}", request.getGsm(), smsResponse.getMessage());
                webSocketController.notifyClient(request.getCardNumber(),
                        "Failed to send OTP, please try again",
                        500);
            }
        } else {
            logger.info("Received verification request for cardholder " + request.getNationalId());
            webSocketController.notifyClient(request.getCardNumber(),
                    "Verification failed",
                    404);
        }
    }


    public boolean checkIfExists(String cardHash, String finalDate, String nationalId, String gsm) throws Exception {
        // Fetch the cardholder based on the cardHash (indexed for fast lookup)
        TabCardHolder cardholder = cardholderRepository.findByCardHash(cardHash);

        // If no cardholder is found, log and return false immediately (early exit)
        if (cardholder == null) {
            logger.info("Verification failed for hashcard: " + cardHash);
            return false;
        }

        // Compare the other attributes in-memory (finalDate, nationalId, gsm)
        boolean isValid = cardholder.getFinalDate().equals(finalDate)
                && cardholder.getNationalId().equals(nationalId)
                && cardholder.getGsm().equals(gsm);

        // If validation fails, log the error message
        if (!isValid) {
            logger.info("Verification failed: Data mismatch for cardholder " + cardHash);

        }

        return isValid; // Return the result of the in-memory comparison
    }






    public boolean isCardholderAndUserInSameBank(String cardHash, String authenticatedUserBankCode) {
        // Fetch the bank code associated with the card hash
        String cardholderBankCode = cardholderRepository.findBankCodeByCardHash(cardHash);


            // If no cardholder is found or the bank code is null, return false and log a warning
            if (cardholderBankCode == null || cardholderBankCode.isEmpty()) {
                logger.warn("Cardholder not found for card hash: {}", cardHash);
                return false; // Return false when the cardholder is not found
            }

            // Compare the cardholder's bank code with the authenticated user's bank code
            if (cardholderBankCode.equals(authenticatedUserBankCode)) {
                logger.info("Cardholder is in the same bank as the authenticated user.");
                return true; // Return true if the bank codes match
            } else {
                logger.warn("Bank code mismatch: Cardholder bank code {} does not match authenticated user's bank code {}",
                        cardholderBankCode, authenticatedUserBankCode);
                return false; // Return false if the bank codes do not match
            }
        }
}
