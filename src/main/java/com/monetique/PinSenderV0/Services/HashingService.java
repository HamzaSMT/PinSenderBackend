package com.monetique.PinSenderV0.Services;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
@Service
public class HashingService {

    public String hashPAN(String pan) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(pan.getBytes());
        StringBuilder hash = new StringBuilder();
        for (byte b : hashBytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }
}

