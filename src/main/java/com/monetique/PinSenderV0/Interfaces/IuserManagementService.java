package com.monetique.PinSenderV0.Interfaces;

public interface IuserManagementService {
    String generateRandomPassword(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
