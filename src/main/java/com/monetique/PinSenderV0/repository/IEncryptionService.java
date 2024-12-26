package com.monetique.PinSenderV0.repository;

public interface IEncryptionService {
    String encrypt(String data) throws Exception;

    String decrypt(String encryptedData) throws Exception;
}
