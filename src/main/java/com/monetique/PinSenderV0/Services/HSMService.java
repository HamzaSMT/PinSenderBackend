package com.monetique.PinSenderV0.Services;


import com.monetique.PinSenderV0.HSM.HSMCalculPinService;
import com.monetique.PinSenderV0.Interfaces.ICardholderService;
import com.monetique.PinSenderV0.models.Banks.TabCardHolder;
import com.monetique.PinSenderV0.payload.request.PinRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;

@Service
public class HSMService {
    @Autowired
    ICardholderService cardholderService;
    @Autowired
    HSMCalculPinService hsmCalculPinService;


    public PinRequest getPinRequestFromCardNumber(String cardNumber) {
        // Cherche le titulaire de la carte par le numéro de carte
        TabCardHolder cardholder = cardholderService.getCardHolderByCardNumber(cardNumber);

        if (cardholder!= null) {

            PinRequest pinRequest = new PinRequest();
            pinRequest.setPvka(cardholder.getBin().getKeyData());
            pinRequest.setOffset(cardholder.getPinOffset());
            pinRequest.setPinLength("4");
            String right12Pan = cardNumber.substring(cardNumber.length() - 13, cardNumber.length() - 1);
            pinRequest.setRight12Pan(right12Pan);
            String pan10 = cardNumber.substring(0, 10);
            pinRequest.setPan10(pan10);
            String decimTable = "0123456789";
            pinRequest.setDecimTable(decimTable);
            return pinRequest;
        } else {
            throw new RuntimeException("Cardholder not found with card number: " + cardNumber);
        }
    }
    public String generateEncryptedPin(String cardNumber) {
        // Extraire les paramètres nécessaires du cardNumber
        PinRequest pinRequest = getPinRequestFromCardNumber(cardNumber);


        String pvka = pinRequest.getPvka();
        String offset = pinRequest.getOffset();
        String pinLength = pinRequest.getPinLength();
        String right12Pan = pinRequest.getRight12Pan();
        String decimTable = pinRequest.getDecimTable();
        String pan10 = pinRequest.getPan10();

        try {
            return hsmCalculPinService.calculateEncryptedPin(pvka, offset, pinLength, right12Pan, decimTable, pan10);
        } catch (IOException e) {
            throw new RuntimeException("Error calculating encrypted PIN", e);
        }
    }
    public String generateClearPin(String cardNumber, String encryptedPin) {
        String right12Pan = cardNumber.substring(cardNumber.length() - 13, cardNumber.length() - 1);

        try {
            return hsmCalculPinService.calculateClearPin(right12Pan, encryptedPin);
        } catch (IOException e) {
            throw new RuntimeException("Error calculating clear PIN", e);
        }
    }


}