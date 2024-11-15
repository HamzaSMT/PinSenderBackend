package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.IuserManagementService;
import com.monetique.PinSenderV0.models.Users.ERole;
import com.monetique.PinSenderV0.models.Users.Role;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.ChangePasswordRequest;
import com.monetique.PinSenderV0.payload.request.GeneratePasswordRequest;
import com.monetique.PinSenderV0.payload.request.SignupRequest;
import com.monetique.PinSenderV0.payload.request.UserUpdateRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.UserResponseDTO;
import com.monetique.PinSenderV0.repository.RoleRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;


@RestController
@RequestMapping("/api/users")
public class UserManagementController {


    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
    @Autowired
    private IuserManagementService userManagementService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder encoder;

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("User is not authenticated!", 401));
            }
            userManagementService.toggleUserActiveStatus(id);
            return ResponseEntity.ok(new MessageResponse("User and associated users status changed successfully.", 200));
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied", 403));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found", 404));
        } catch (Exception e) {
            logger.error("Error during user deactivation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Internal server error during user deactivation", 500));
        }
    }


    @PostMapping("/associateAdminToBank")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> associateAdminToBank(@RequestParam Long adminId, @RequestParam Long bankId) {
        logger.info("Received request to associate admin {} with bank {}", adminId, bankId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
            throw new AccessDeniedException("Error: Only Super Admin can associate Admins with Banks.");
        }

        try {
            // Call the service to associate the admin with the bank
            userManagementService.associateAdminWithBank(adminId, bankId);
            logger.info("Admin {} successfully associated with bank {}", adminId, bankId);
            return ResponseEntity.ok(new MessageResponse("Admin successfully associated with the bank!", 200));
        } catch (AccessDeniedException e) {
            logger.error("bad data: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Resource not found: " + e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Unexpected error while associating admin to bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error associating admin to bank", 500));
        }
    }


    @PostMapping("/associateUserToAgency")
    public ResponseEntity<?> associateUserToAgency(@RequestParam Long userId, @RequestParam Long agencyId) {
        logger.info("Received request to associate user {} with agency {}", userId, agencyId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentAdmin = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", currentUserDetails.getId()));

        try {
            // Call the service to associate the user with the agency
            userManagementService.associateUserWithAgency(userId, agencyId, currentAdmin);
            logger.info("User {} successfully associated with agency {}", userId, agencyId);
            return ResponseEntity.ok(new MessageResponse("User successfully associated with the agency!", 200));
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Resource not found: " + e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Unexpected error while associating user to agency: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error associating user to agency", 500));
        }
    }


    // Create Super Admin method


    // Signup method (Register)
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Received sign-up request for username: {}", signUpRequest.getUsername());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            logger.error("Username {} is already taken", signUpRequest.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!", 400));
        }


        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        try {
            if (strRoles == null || strRoles.isEmpty()) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
                                throw new AccessDeniedException("Error: Only Super Admins can create Admins.");
                            }
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));
                            roles.add(adminRole);

                            // Create Admin without mandatory bank association initially
                            User adminUser = new User(signUpRequest.getUsername(), encoder.encode(signUpRequest.getPassword()),
                                    roles, currentUser,null, null); // No bank, no agency
                            userRepository.save(adminUser);
                            logger.info("Admin {} created successfully", signUpRequest.getUsername());
                            break;

                        case "user":
                            if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_ADMIN))) {
                                throw new AccessDeniedException("Error: Only Admins can create Users.");
                            }
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
                            roles.add(userRole);

                            // Create User without mandatory bank and agency association initially
                            User user = new User(signUpRequest.getUsername(), encoder.encode(signUpRequest.getPassword()),
                                    roles, currentUser, currentUser.getBank(), null); // Auto-associated to Admin's bank
                            userRepository.save(user);
                            logger.info("User {} created successfully", signUpRequest.getUsername());
                            break;

                        default:
                            throw new AccessDeniedException("Error: Role not recognized.");
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Error during user registration: {}", signUpRequest.getUsername(), e);
            return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to register user", 500));
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!", 200));
    }


    @PostMapping("/changePassword")

    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }
        try {
            userManagementService.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully!", 200));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new MessageResponse(e.getMessage(), 400));
        }
    }
    @PostMapping("/forgetPassword")
    public ResponseEntity<?> generateRandomPassword(@RequestBody GeneratePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
            }
            if (request == null || request.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("User ID is required.", 400));
            }

            try {
                // Generate a random password for the specified user
                String newPassword = userManagementService.generateRandomPassword(request.getUserId());

                // Check if password generation was successful
                if (newPassword == null || newPassword.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse("Failed to generate a new password.", 500));
                }

                // Successfully generated and saved the password
                return ResponseEntity.ok(newPassword);

            } catch (ResourceNotFoundException e) {
                // Handle the case where the user is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("User not found with ID: " + request.getUserId(), 404));

            } catch (Exception e) {
                // Handle any other unexpected exceptions
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new MessageResponse("An error occurred: " + e.getMessage(), 500));
            }
        }



    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest updateUserRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }
        try {
            // Get authenticated user details

            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = currentUserDetails.getId();
            // Update user details
            User updatedUser = userManagementService.updateUser(userId, updateUserRequest);
            logger.info("User {} updated successfully", updatedUser.getUsername());

            // Return success message
            return ResponseEntity.ok(new MessageResponse("User updated successfully!", 200));
        } catch (ResourceNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error updating user details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating user details", 500));
        }
    }
    @GetMapping("/users")
    // Ensure only admins can access this endpoint
    public ResponseEntity<?> getUsersByAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }
        try {
            List<UserResponseDTO> users = userManagementService.getUsersByAdmin();

            // Successful response with users list
            return ResponseEntity.ok(users);
        } catch (ResourceNotFoundException e) {
            // Handle case when no users are found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage(), 404));
        } catch (IllegalStateException e) {
            // Handle case when user is not authenticated
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated", 401));
        } catch (Exception e) {
            // Handle any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving users", 500));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long userId) {
        logger.info("Received request to get user by ID: {}", userId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }
        try {
            User user = userManagementService.getuserbyId(userId);
            logger.info("User found: {}", user);
            return ResponseEntity.ok(user); // Return the user object if found
        } catch (NoSuchElementException e) {
            logger.error("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found", 404)); // 404 Not Found
        } catch (Exception e) {
            logger.error("Error retrieving user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving user", 500)); // 500 Internal Server Error
        }
    }


}
