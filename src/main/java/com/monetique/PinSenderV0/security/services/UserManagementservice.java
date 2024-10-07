package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Users.Role;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.UserUpdateRequest;
import com.monetique.PinSenderV0.payload.response.InvalidPasswordException;
import com.monetique.PinSenderV0.payload.response.UserResponseDTO;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.Interfaces.IuserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserManagementservice implements IuserManagementService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;


    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

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

    @Override
    public User getuserbyId(Long userId) {
        return userRepository.findById(userId).get();
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
    @Override
    public List<UserResponseDTO> getUsersByAdmin() {
        // Get the authenticated user
        logger.info("Fetching users by admin");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if authentication exists
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Admin is not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        logger.info("Admin ID from authenticated user: " + adminId);

        // Fetch users where admin_id equals the connected user's ID
        List<User> users = userRepository.findByAdminId(adminId);
        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found for the admin with ID " + adminId);
        }

        // Map user entities to response DTOs with only necessary data
        List<UserResponseDTO> responseList = users.stream().map(user -> {
            UserResponseDTO response = new UserResponseDTO();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            //response.setPassword(user.getPassword());
            response.setEmail(user.getEmail());
            response.setPhoneNumber(user.getPhoneNumber());

                    if (!user.getRoles().isEmpty()) {
                        response.setRole(user.getRoles().iterator().next().getName().toString());
                    } else {
                        response.setRole("No Role Assigned");
                    }

                    response.setBank(user.getBank());
                    return response;
                })
                .collect(Collectors.toList());


        logger.info("All users are listed for admin ID: {}", adminId);
        return responseList;



    }
}
