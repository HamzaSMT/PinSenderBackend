package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.UserUpdateRequest;
import com.monetique.PinSenderV0.payload.response.UserResponseDTO;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface IuserManagementService {
    String generateRandomPassword(Long userId);

    User getuserbyId(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    User updateUser(Long userId, UserUpdateRequest userUpdateRequest);

    List<UserResponseDTO> getUsersByAdmin();

    void associateAdminWithBank(Long adminId, Long bankId)
                throws ResourceNotFoundException, AccessDeniedException;

    void associateUserWithAgency(Long userId, Long agencyId, User currentAdmin)
            throws ResourceNotFoundException, AccessDeniedException;



    void toggleUserActiveStatus(Long userId);
}
