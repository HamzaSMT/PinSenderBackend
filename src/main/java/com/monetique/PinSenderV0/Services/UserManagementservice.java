package com.monetique.PinSenderV0.Services;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Services.managementbank.BankService;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.ERole;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.UserUpdateRequest;
import com.monetique.PinSenderV0.payload.response.InvalidPasswordException;
import com.monetique.PinSenderV0.payload.response.UserResponseDTO;
import com.monetique.PinSenderV0.payload.response.UserbyidResponseDTO;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.Interfaces.IuserManagementService;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserManagementservice implements IuserManagementService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;


    private static final Logger logger = LoggerFactory.getLogger(BankService.class);
    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private AgencyRepository agencyRepository;

    @Override
    public String generateRandomPassword(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long adminId = userDetails.getId();

        // Find the user for whom the password needs to be reset
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Ensure the authenticated user is authorized to reset this user's password
        if (user.getAdmin() == null || !user.getAdmin().getId().equals(adminId)) {
            throw new AccessDeniedException("You are not authorized to reset the password for this user.");
        }

        // Generate a strong random password
        String newPassword = generateRandomPassword();

        // Encrypt the password before saving
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        // Return the plain generated password (be cautious with exposing this in production)
        return newPassword;
    }
    @Override
    public String generateRandomPasswordSuperadmin(Long userId) {

        // Find the user for whom the password needs to be reset
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!user.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
            throw new AccessDeniedException("This API is only for the super admin.");
        }

        // Generate a strong random password
        String newPassword = generateRandomPassword();

        // Encrypt the password before saving
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        // Return the plain generated password (be cautious with exposing this in production)
        return newPassword;
    }

    @Override
    public UserbyidResponseDTO getuserbyId(Long userId) {
        // Récupère l'utilisateur par son ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Crée le DTO et y mappe les informations
        UserbyidResponseDTO responseDTO = new UserbyidResponseDTO();
        responseDTO.setId(user.getId());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setEmail(user.getEmail());
        responseDTO.setPhoneNumber(user.getPhoneNumber());
        responseDTO.setActive(user.isActive());

        // Mappage des rôles
        responseDTO.setRoles(user.getRoles());

        // Mappage de l'adminId (si nécessaire)
        if (user.getAdmin() != null) {
            responseDTO.setAdminId(user.getAdmin().getId());
        }

        // Mappage de la banque et de l'agence
        responseDTO.setBank(user.getBank());
        responseDTO.setAgency(user.getAgency());

        return responseDTO;
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
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();

        // Find the user by the authenticated user's ID
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", authenticatedUserId));

        // Verify the old password
        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect.");
        }

        // Set the new password
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
                    response.setStatus(user.isActive());
                    response.setEmail(user.getEmail());
                    response.setPhoneNumber(user.getPhoneNumber());

                    if (!user.getRoles().isEmpty()) {
                        response.setRole(user.getRoles().iterator().next().getName().toString());
                    } else {
                        response.setRole("No Role Assigned");
                    }
                    if (user.getBank() != null) {
                        response.setBankName(user.getBank().getName());
                        response.setBankCode(user.getBank().getBankCode());
                        response.setLogoContent(user.getBank().getLogoContent());

                    } else {
                        response.setBankName("No bank Assigned");
                        response.setBankCode("No bank Assigned");
                        response.setLogoContent(null);


                    }
                    return response;
                })
                .collect(Collectors.toList());


        logger.info("All users are listed for admin ID: {}", adminId);
        return responseList;

    }

    @Override
    public void associateAdminWithBank(Long adminId, Long bankId)
            throws ResourceNotFoundException, AccessDeniedException {
        // Fetch the admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));
        // Check if the admin already has a bank associated
        if (admin.getBank() != null) {
            throw new AccessDeniedException("Error: This admin already has a bank associated and cannot change it!");
        }
        // Fetch the bank
        TabBank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", bankId));

        // Associate the admin with the bank
        admin.setBank(bank);
        userRepository.save(admin);
        // Update the bank with the admin's username
        bank.setAdminUsername(admin.getUsername());
        bankRepository.save(bank);
    }

    @Override
    public void associateUserWithAgency(Long userId, Long agencyId, User currentAdmin)
            throws ResourceNotFoundException, AccessDeniedException {

        // Fetch the user to be associated
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if the user belongs to the current admin
        if (!user.getAdmin().getId().equals(currentAdmin.getId())) {
            throw new AccessDeniedException("Error: You can only associate your own users.");
        }

        // Fetch the agency
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", agencyId));

        // Check if the agency belongs to the same bank as the current admin
        if (!agency.getBank().getId().equals(currentAdmin.getBank().getId())) {
            throw new AccessDeniedException("Error: You can only associate users with agencies in your bank.");
        }

        // Associate the user with the agency
        user.setAgency(agency);
        userRepository.save(user);
    }

    @Override
    public void toggleUserActiveStatus(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long adminId = userDetails.getId();
        logger.error("No authentication found in SecurityContext");
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for ID: " + userId));

            if (user.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
                throw new AccessDeniedException("You are not authorized to modify this user.");
            }
            // Vérifier si l'admin actuel est autorisé à modifier ce statut utilisateur
            if (user.getAdmin()!= null && !user.getAdmin().getId().equals(adminId)) {
                logger.info("admin super{}", user.getAdmin());
                logger.info("User connecte id {}", user.getAdmin().getId().equals(adminId));
                throw new AccessDeniedException("You are not authorized to modify this user.");

            }


            logger.info("Roles for user {}: {}", userId, user.getRoles());

            // Toggle du statut actif
            boolean newStatus = !user.isActive();
            user.setActive(newStatus);
            userRepository.save(user);
            logger.info("User {} active status set to: {}", user.getUsername(), newStatus);

            // Si l'utilisateur est un admin, modifier le statut de tous les utilisateurs associés
            if (isAdmin(user)) {
                logger.info("Toggling active status for associated users of admin: {}", user.getUsername());
                toggleAssociatedUsersStatus(user, newStatus);
            }

        } catch (AccessDeniedException ex) {
            logger.warn("Access denied");
            throw ex;
        } catch (ResourceNotFoundException ex) {
            logger.warn("Resource not found");
            throw ex;
        } catch (Exception ex) {
            logger.error("Unexpected error occurred while toggling user status.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected issue occurred. Please contact support.");
        }
    }


    // Helper method to check if the user has an admin role
    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(ERole.ROLE_ADMIN));
    }
    // Helper method to deactivate all users associated with an admin
    private void toggleAssociatedUsersStatus(User admin, boolean newStatus) {
        List<User> associatedUsers = userRepository.findByAdminId(admin.getId());
        for (User user : associatedUsers) {
            System.out.println("Setting active status for associated user: " + user.getUsername() + " to " + newStatus);
            user.setActive(newStatus);
            userRepository.save(user);
        }
    }
}
