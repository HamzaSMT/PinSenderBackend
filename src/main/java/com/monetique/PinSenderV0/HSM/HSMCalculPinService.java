package com.monetique.PinSenderV0.HSM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class HSMCalculPinService {
    private static final Logger logger = LoggerFactory.getLogger(HSMCommunication.class);
    @Autowired
    private HSMCommunication hsmCommunication;

    // Calcul du PIN chiffré
    public String calculateEncryptedPin(String pvka, String offset, String pinLength, String right12Pan, String decimTable, String pan10) throws IOException {
        String request = "MHDR" + "EE" + pvka + offset + pinLength + right12Pan + decimTable + pan10;
        logger.info("request");
        hsmCommunication.connect();  // IP et port du HSM
        logger.info("connection");
        hsmCommunication.sendCommand(request);
        logger.info("send command");
        String response = hsmCommunication.getResponse();
        logger.info(response);
        hsmCommunication.close();
        logger.info("close");

        if (response.startsWith("EF") && response.substring(2, 4).equals("00")) {
            return response.substring(4, 13);  // Le PIN chiffré commence à la position 5
        } else {
            throw new RuntimeException("Erreur dans la réponse du HSM : " + response);
        }
    }

    // Calcul du PIN en clair
    public String calculateClearPin(String right12Pan, String encryptedPin) throws IOException {
        String request = "MHDR" + "NG" + right12Pan + encryptedPin;

        hsmCommunication.connect();  // IP et port du HSM
        logger.info("connection");
        hsmCommunication.sendCommand(request);
        logger.info("send command");
        String response = hsmCommunication.getResponse();
        logger.info("response" +response);
        hsmCommunication.close();
        logger.info("close");

        if (response.startsWith("NH") && response.substring(2, 4).equals("00")) {
            return response.substring(4, 52);  // Le PIN clair commence à la position 5
        } else {
            throw new RuntimeException("Erreur dans la réponse du HSM : " + response);
        }
    }
}

