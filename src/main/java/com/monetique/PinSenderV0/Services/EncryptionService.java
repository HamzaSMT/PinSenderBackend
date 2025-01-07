package com.monetique.PinSenderV0.Services;

import com.monetique.PinSenderV0.Interfaces.IEncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService implements IEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final byte[] SECRET_KEY = "7f3b22f68f3842c6be7e3b9b28a79bc0".getBytes(); // 32 bytes for AES-256

    @Override
    public String encrypt(String data) {
        if (data == null) {
            return null; // Return null if input data is null
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            // Log the exception and return null or throw a RuntimeException
            e.printStackTrace(); // Replace with proper logging in production
            return null; // Optionally, throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        if (encryptedData == null) {
            return null; // Return null if input data is null
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] originalData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(originalData);
        } catch (Exception e) {
            // Log the exception and return null or throw a RuntimeException
            e.printStackTrace(); // Replace with proper logging in production
            return null; // Optionally, throw new RuntimeException("Decryption failed", e);
        }
    }
}

