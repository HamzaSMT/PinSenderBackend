package com.monetique.PinSenderV0.Interfaces;

public interface IEncryptionService {
    String encrypt(String data) ;

    String decrypt(String encryptedData) ;
}
