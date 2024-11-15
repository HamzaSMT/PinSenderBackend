package com.monetique.PinSenderV0.HSM;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
    private String request;

    @Value("${hsm.ip}")
    private String hsmIp;

    @Value("${hsm.port}")
    private int hsmPort;

    public void connect(String hsmIp, int hsmPort) throws IOException {
        try {
            socket = new Socket(hsmIp, hsmPort);
            System.out.println("Successfully connected to HSM ");
            socket.setSoTimeout(5000);
        } catch (IOException e) {
            throw new IOException("Connection error to HSM: " + e.getMessage());
        }
    }

    public void sendCommand() throws IOException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Socket is not connected.");
        }

        try {
            // Prepare the command bytes array
            byte[] commandBytes = new byte[255];
            commandBytes[0] = 0;  // Placeholder for identifier
            commandBytes[1] = (byte) request.length();  // Length of the command

            // Fill the command bytes
            byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(requestBytes, 0, commandBytes, 2, requestBytes.length);

            // Fill remaining bytes with 0
            for (int i = requestBytes.length + 2; i < commandBytes.length; i++) {
                commandBytes[i] = 0;
            }

            // Log the command being sent for debugging
            logger.info("Sending command (hex)");

            // Send the command
            OutputStream out = socket.getOutputStream();
            out.write(commandBytes);
            out.flush();  // Ensure all data is sent
            logger.info("Command sent successfully.");

            // Wait for response
            InputStream in = socket.getInputStream();
            byte[] responseBuffer = new byte[1024];  // Adjust size as necessary
            int bytesRead = in.read(responseBuffer);

            if (bytesRead > 0) {
                response = new String(responseBuffer, 0, bytesRead, StandardCharsets.US_ASCII);
                logger.info("Received response: {}", response);
            } else {
                logger.warn("No data received from HSM.");
            }
        } catch (IOException e) {
            throw new IOException("Error sending command to HSM: " + e.getMessage());
        }
    }


    public String getResponse() {
        return response.replaceAll("[^\\p{Print}]", "").trim();  // Supprime les caractères non imprimables
    }


    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                throw new IOException("Error closing HSM connection: " + e.getMessage());
            }
        }
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
