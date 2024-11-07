package com.monetique.PinSenderV0.controllers;


import com.monetique.PinSenderV0.HSM.HSMCommunication;
import com.monetique.PinSenderV0.Services.HSMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/cardholders")
public class hsmtest {
    @Autowired
    private HSMService hsmService;
    @Autowired
    private HSMCommunication hsmCommunication;


    @PostMapping("/test")
    public String testHsmService(@RequestParam String cardNumber) {

        String encryptedPin = hsmService.generateEncryptedPin(cardNumber);

        String clearPin = hsmService.generateClearPin(cardNumber, encryptedPin);

        return "PIN calculer avec succès au numéro : " + clearPin +"   "+ encryptedPin;


    }
    @PostMapping("/calculatePin")
    public String calculatePin(@RequestParam String cardNumber) {
        try {
            // 1. Calculer le PIN chiffré à partir du numéro de carte
            String encryptedPin = hsmService.generateEncryptedPin(cardNumber);

            // 2. Calculer le PIN en clair à partir du PIN chiffré
            String clearPin = hsmService.generateClearPin(cardNumber, encryptedPin);

            return "PIN en clair : " + clearPin;
        } catch (Exception e) {
            return "Erreur lors du calcul du PIN : " + e.getMessage();
        }
    }


/*
    @PostMapping("/conect")
    public String connectHsmService() {
            try {
                // 1. Connecter au HSM
                hsmCommunication.connect();

                String response = hsmCommunication.getResponse();

                // 4. Fermer la connexion
                hsmCommunication.close();

                return "Réponse du HSM : " + response;
            } catch (IOException e) {
                return "Erreur lors de la communication avec le HSM : " + e.getMessage();
            }
        }*/

}
