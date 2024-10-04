/*package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.repository.CardholderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CardholderService {

    @Autowired
    private CardholderRepository cardholderRepository;

    @Autowired
    private InfobipSmsService infobipSmsService;

    @Autowired
    BillingServicePinOtp billingService;

    public boolean verifyCardholder(String cardNumber, String cin, String phoneNumber, String expirationDate) {
        Optional<Cardholder> cardholder = cardholderRepository.findByCardNumberAndCinAndPhoneNumberAndExpirationDate(
                cardNumber, cin, phoneNumber,expirationDate
        );

        return cardholder.isPresent() && cardholder.get().getExpirationDate().equals(expirationDate);
    }

    public void sendPin(String phoneNumber) {
        // Retrieve all cardholders with the given phone number
        List<Cardholder> cardholders = cardholderRepository.findAllByPhoneNumber(phoneNumber);

        if (cardholders.size() == 1) {
            String pin = cardholders.get(0).getPin();
            try {
                infobipSmsService.sendSms(phoneNumber, "Your card PIN is: " + pin);


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (cardholders.isEmpty()) {
            System.err.println("No cardholder found with phone number " + phoneNumber);
        } else {
            System.err.println("Multiple cardholders found with phone number " + phoneNumber);
        }
    }

}
*/