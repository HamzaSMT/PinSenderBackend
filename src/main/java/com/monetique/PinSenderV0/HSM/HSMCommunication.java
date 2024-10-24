package com.monetique.PinSenderV0.HSM;

import java.io.*;
import java.net.Socket;

import com.monetique.PinSenderV0.Services.HSMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HSMCommunication {
    private static final Logger logger = LoggerFactory.getLogger(HSMCommunication.class);
    private Socket socket;
    private String response;

    @Value("${hsm.ip}")
    private String hsmIp;

    @Value("${hsm.port}")
    private int hsmPort;

    public void connect() throws IOException {
        try {
            socket = new Socket(hsmIp, hsmPort);
            System.out.println("Connected to " + hsmIp + ":" + hsmPort);
            socket.setSoTimeout(5000);  // Timeout
            logger.info("connect");
        } catch (IOException e) {
            throw new IOException("Erreur de connexion au HSM : " + e.getMessage());
        }
    }

    public void sendCommand(String request) throws IOException {
        logger.info("sendCommand");
        try (OutputStream out = socket.getOutputStream(); InputStream in = socket.getInputStream()) {
            logger.info("OutputStream +InputStream");
            byte[] requestBytes = request.getBytes();
            out.write(requestBytes);
            out.flush();
            byte[] buffer = new byte[256];
            int bytesRead = in.read(buffer);
            response = new String(buffer, 0, bytesRead);
            logger.info(response);
        } catch (IOException e) {
            throw new IOException("Erreur lors de l'envoi de la commande au HSM : " + e.getMessage());
        }
    }

    public String getResponse() {
        return response;
    }

    public void close() throws IOException {
        try {
            socket.close();
        } catch (IOException e) {
            throw new IOException("Erreur lors de la fermeture de la connexion HSM : " + e.getMessage());
        }
    }
}
