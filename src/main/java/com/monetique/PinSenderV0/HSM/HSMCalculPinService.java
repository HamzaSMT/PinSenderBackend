package com.monetique.PinSenderV0.HSM;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class HSMCalculPinService {
    private static final Logger logger = LoggerFactory.getLogger(HSMCalculPinService.class);
    @Value("${hsm.ip}")
    private String hsmIp;

    @Value("${hsm.port}")
    private int hsmPort;
    @Autowired
    private HSMCommunication hsmCommunication;

    // Calcul du PIN chiffré
    public String calculateEncryptedPin(String pvka, String offset, String pinLength, String right12Pan, String decimTable, String pan10) throws IOException {
        String request = "MHDR" + "EE" + pvka + offset + pinLength + right12Pan + decimTable + pan10+"NF";
        logger.info("Preparing request for encrypted PIN");

        try {
            hsmCommunication.connect(hsmIp, hsmPort);  // Actual IP and port
            logger.info("Successfully connected to HSM.");
            hsmCommunication.setRequest(request);
            hsmCommunication.sendCommand();
            logger.info("Command sent.");

            String response = hsmCommunication.getResponse();
            logger.info("Received response: ");

            // Additional detailed logging for debugging
            if (response.length() >= 13) {
                String status = response.substring(4,6);
                String resultCode = response.substring(6, 8);
                String encryptedPin = response.substring(8, response.length());
                logger.info("Status code: {}", status);
                logger.info("Result code: {}", resultCode);
                logger.info("Encrypted PIN extracted: ");

                if ("EF".equals(status) && "00".equals(resultCode)) {
                    return encryptedPin;
                }
            }

            throw new RuntimeException("Erreur dans la réponse du HSM : " + response);
        } catch (IOException e) {
            logger.error("Error during HSM communication: {}", e.getMessage());
            throw e;
        } finally {
            hsmCommunication.close();
            logger.info("Connection closed.");
        }
    }


    // Calcul du PIN en clair
    public String calculateClearPin(String right12Pan, String encryptedPin) throws IOException {
        String request = "MHDR" + "NG" + right12Pan + encryptedPin;
        logger.info("Preparing request for clear PIN");

        try {
            hsmCommunication.connect(hsmIp, hsmPort);  // Provide actual IP and port
            logger.info("Successfully connected to HSM.");
            hsmCommunication.setRequest(request);  // Set the request before sending
            hsmCommunication.sendCommand();
            logger.info("Command sent.");
            String response = hsmCommunication.getResponse();
            logger.info("Received response");
            String status = response.substring(4,6);
            String resultCode = response.substring(6, 8);
            String Pin = response.substring(8, 12);
            logger.info("Status code: {}", status);
            logger.info("Result code: {}", resultCode);
            logger.info("Encrypted PIN extracted");
            if ("NH".equals(status) && "00".equals(resultCode)) {
                return Pin;  // Le PIN clair
            } else {
                throw new RuntimeException("Erreur dans la réponse du HSM : " + response);
            }
        } catch (IOException e) {
            logger.error("Error during HSM communication: {}", e.getMessage());
            throw e;
        } finally {
            hsmCommunication.close();
            logger.info("Connection closed.");
        }
    }
}


