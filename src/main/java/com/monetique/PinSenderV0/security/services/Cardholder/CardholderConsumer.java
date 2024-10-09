package com.monetique.PinSenderV0.security.services.Cardholder;


import com.monetique.PinSenderV0.Interfaces.IOtpService;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;

import com.monetique.PinSenderV0.repository.TabCardHolderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardholderConsumer {

    @Autowired
    private TabCardHolderRepository cardholderRepository;
    @Autowired
    private IOtpService otpService;

    @RabbitListener(queues = "cardholder.queue")
    public void handleMessage(VerifyCardholderRequest request) {
        // Simulate processing the verification
        System.out.println("Received verification request for cardholder: " + request.getCardNumber());

        // Example logic: Check cardholder details
        boolean valid = cardholderRepository.existsByCardNumberAndFinalDateAndNationalIdAndGsm(
                request.getCardNumber(),
                request.getFinalDate(),
                request.getNationalId(),
                request.getGsm()
        );

        if (valid) {
            System.out.println("Cardholder verified successfully for GSM: " + request.getGsm());
            // Proceed with OTP generation, sending SMS, etc.

            String otp = otpService.sendOtp(request.getGsm());
            System.out.println("OTP sent to phone number: " + request.getGsm()+"with value"+ otp);

            // Here you can wait for the user to input the OTP or return a success response
        } else {
            System.out.println("Verification failed for cardholder: " + request.getCardNumber());
        }
    }
}
