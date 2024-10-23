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
    HSMService hsmService;
    @Autowired
    HSMCommunication hsmCommunication;


    @PostMapping("/test")
    public String testHsmService(@RequestParam String cardNumber) {

        String encryptedPin = hsmService.generateEncryptedPin(cardNumber);
        System.out.println(encryptedPin);

        String clearPin = hsmService.generateClearPin(cardNumber, encryptedPin);
        System.out.println(clearPin);

        return "PIN calculer avec succès au numéro : " + clearPin + encryptedPin;


    }
    @PostMapping("/conect")
    public String connectHsmService(@RequestParam String cardNumber) {
            try {
                // 1. Connecter au HSM
                hsmCommunication.connect();

                // 2. Envoyer la commande reçue via la requête


                // 3. Obtenir la réponse du HSM
                String response = hsmCommunication.getResponse();

                // 4. Fermer la connexion
                hsmCommunication.close();

                return "Réponse du HSM : " + response;
            } catch (IOException e) {
                return "Erreur lors de la communication avec le HSM : " + e.getMessage();
            }
        }
}
