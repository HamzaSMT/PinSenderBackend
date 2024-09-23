package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.UserUpdateRequest;
import com.monetique.PinSenderV0.payload.response.InvalidPasswordException;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.Interfaces.IuserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class UserManagementservice implements IuserManagementService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;




    @Override
    public String generateRandomPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String newPassword = generateRandomPassword();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        // Return the generated password to be used in the response
        return newPassword;
    }

    public String generateRandomPassword() {
        // Define the length of the password and characters to be included
        int length = 12;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect.");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

@Override
public User updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        // Find user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Update fields
        user.setEmail(userUpdateRequest.getEmail());
        user.setPhoneNumber(userUpdateRequest.getPhoneNumber());

        // Save updated user
        return userRepository.save(user);
    }


}
