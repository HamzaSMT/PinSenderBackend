package com.monetique.PinSenderV0.security.services;


import com.monetique.PinSenderV0.Interfaces.ICardholderService;
import com.monetique.PinSenderV0.models.Banks.TabCardHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class HSMService {
    @Autowired
    ICardholderService cardholderService;

    private static final SecureRandom secureRandom = new SecureRandom();

    // Méthode pour générer un PIN à 4 chiffres
    public String generatePin(String cardNumber) {

        TabCardHolder tabCardHolder = cardholderService.getCardHolderByCardNumber(cardNumber);

        // Générer un nombre entier aléatoire entre 0 et 9999
        int pin = secureRandom.nextInt(10000);  // Limité à 4 chiffres

        // Formater le PIN avec des zéros devant s'il est inférieur à 1000 (ex. 0001)
        return String.format("%04d", pin);  // Format sur 4 chiffres
    }

}
