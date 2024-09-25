package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.UserUpdateRequest;
import com.monetique.PinSenderV0.payload.response.UserResponseDTO;

import java.util.List;

public interface IuserManagementService {
    String generateRandomPassword(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    User updateUser(Long userId, UserUpdateRequest userUpdateRequest);

    List<UserResponseDTO> getUsersByAdmin();
}
