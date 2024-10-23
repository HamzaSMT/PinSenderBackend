package com.monetique.PinSenderV0.controllers;


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


    @PostMapping("/test")
    public String testHsmService(@RequestParam String cardNumber) {

        String encryptedPin = hsmService.generateEncryptedPin(cardNumber);
        System.out.println(encryptedPin);

        String clearPin = hsmService.generateClearPin(cardNumber, encryptedPin);
        System.out.println(clearPin);

        return "PIN calculer avec succès au numéro : " + clearPin + encryptedPin;


    }
}
