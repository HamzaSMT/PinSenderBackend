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
            System.out.println("Successfully connected to HSM at " + hsmIp + ":" + hsmPort);
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
            commandBytes[0] = 0;  // Placeholder identifier
            commandBytes[1] = (byte) request.length();  // Length of the command

            byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(requestBytes, 0, commandBytes, 2, requestBytes.length);
            // Fill remaining bytes with 0
            for (int i = requestBytes.length + 2; i < commandBytes.length; i++) {
                commandBytes[i] = 0;
            }

            // Send the command
            OutputStream out = socket.getOutputStream();
            out.write(commandBytes);
            out.flush();
            System.out.println("Command sent successfully.");

            // Wait for a response
            Thread.sleep(2000);  // Adjust sleep time as needed

            // Define the response buffer
            byte[] responseBuffer = new byte[1024];  // Adjust size as necessary
            InputStream in = socket.getInputStream();
            int bytesRead = in.read(responseBuffer);

            if (bytesRead > 0) {
                // Convert the response to a string
                response = new String(responseBuffer, 0, bytesRead, StandardCharsets.US_ASCII);
                System.out.println("Received response from HSM: " + response);
            } else {
                System.out.println("No data received from HSM.");
            }
        } catch (IOException | InterruptedException e) {
            throw new IOException("Error during communication with HSM: " + e.getMessage());
        }
    }

    public String getResponse() {
        return response;
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
