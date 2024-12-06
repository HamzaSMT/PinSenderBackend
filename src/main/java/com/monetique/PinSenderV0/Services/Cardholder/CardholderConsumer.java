package com.monetique.PinSenderV0.Services.Cardholder;
import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.Interfaces.IStatisticservices;
import com.monetique.PinSenderV0.Services.HashingService;
import com.monetique.PinSenderV0.controllers.WebSocketController;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.repository.TabCardHolderRepository;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CardholderConsumer {

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
        System.out.println("Received verification request for cardholder: " + request.getCardNumber());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUser = (UserDetailsImpl) authentication.getPrincipal();
        String authenticatedUserBankCode = currentUser.getAdmin().getBank().getBankCode();

        String cardHash = hashingService.hashPAN(request.getCardNumber());
        // Check if the cardholder belongs to the same bank
        boolean isSameBank = isCardholderAndUserInSameBank(cardHash, authenticatedUserBankCode);
        if (!isSameBank) {
            System.out.println("Bank code mismatch: Cardholder and authenticated user belong to different banks.");
            webSocketController.notifyClient(request.getCardNumber(), "Unauthorized access: Cardholder belongs to a different bank.", 403);
            return;
        }

        boolean valid= checkIfExists(cardHash,
                request.getFinalDate(),
                request.getNationalId(),
                request.getGsm());

        if (valid) {
            System.out.println("Cardholder verified successfully for GSM: " + request.getGsm());
            // Proceed with OTP generation, sending SMS, etc.

            String otp = otpService.sendOtp(request.getGsm());
            System.out.println("OTP will be send to phone number: " + request.getGsm()+"with value"+ otp);

            webSocketController.notifyClient(request.getCardNumber(), "Cardholder verified successfully . OTP sent.",200);

            // Log the sent OTP using details from the authenticated user
            statisticservices.logSentItem(request.getAgentId(), request.getBranchId(), request.getBankId(), "OTP");


            // Here you can wait for the user to input the OTP or return a success response
        } else {
            System.out.println("Verification failed for cardholder: " + request.getCardNumber());
            webSocketController.notifyClient(request.getCardNumber(), "Verification failed.",404);

        }
    }


    public boolean checkIfExists(String cardHash, String finalDate, String nationalId, String gsm) throws Exception {
        System.out.println("Verification failed for hashcard: " + cardHash);
        return cardholderRepository.existsByCardHashAndFinalDateAndNationalIdAndGsm(cardHash, finalDate, nationalId, gsm);
    }

    public boolean isCardholderAndUserInSameBank(String cardHash, String authenticatedUserBankCode) {
        Optional<String> cardholderBankCode = cardholderRepository.findBankCodeByCardHash(cardHash);
        return cardholderBankCode.isPresent() && cardholderBankCode.get().equals(authenticatedUserBankCode);
    }
}
