package com.monetique.PinSenderV0.Services.managementbank;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.Iagencyservices;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.AgencyRequest;
import com.monetique.PinSenderV0.payload.response.AgencyDTO;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.UserAgenceDTO;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;

@Service
public class AgencyService implements Iagencyservices {


    private static final Logger logger = LoggerFactory.getLogger(AgencyService.class);

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public MessageResponse createAgency(AgencyRequest agencyRequest, Long userId) {
        logger.info("Creating agency with name: {}", agencyRequest.getName());

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUser.getUsername());
            throw new AccessDeniedException("Only Admins can create agencies.");
        }

        Agency agency = new Agency();
        agency.setAgencyCode(agencyRequest.getAgencyCode());
        agency.setName(agencyRequest.getName());
        agency.setContactEmail(agencyRequest.getContactEmail());
        agency.setContactPhoneNumber(agencyRequest.getContactPhoneNumber());
        agency.setAdresse(agencyRequest.getAdresse());
        agency.setCity(agencyRequest.getCity());
        agency.setRegion(agencyRequest.getRegion());
        agency.setBank(currentUser.getBank());

        agencyRepository.save(agency);

        logger.info("Agency {} created successfully by Admin {}", agencyRequest.getAgencyCode(), currentUser.getUsername());
        return new MessageResponse("Agency created successfully!", 200);
    }
    @Override
    public List<AgencyDTO> listAllAgencies(Long userId) {
        logger.info("Listing all agencies for user id: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Long bankId = currentUser.getBank().getId();
        List<Agency> agencies = agencyRepository.findByBankIdWithUsers(bankId);

        // Convert the list of users to UserAgenceDTO
        return agencies.stream().map(agency -> new AgencyDTO(
                agency.getId(),
                agency.getName(),
                agency.getContactEmail(),
                agency.getAgencyCode(),  // Ensure this is the correct agency code
                agency.getContactPhoneNumber(),
                agency.getAdresse(),
                agency.getRegion(),       // Correctly map region
                agency.getCity(),         // Correctly map city
                agency.getBank() != null ? agency.getBank().getName() : null
        )).collect(Collectors.toList());
    }
    public List<UserAgenceDTO> listAllAgenciesAssociatedUser(Long userId) {
        logger.info("Listing all agencies for user id: {}", userId);
        List<User> users = userRepository.findByAdminId(userId);

        // Convert the list of users to UserAgenceDTO
        return users.stream().map(user -> {
            Agency agency = user.getAgency();
            // Assuming there's only one role per user
            return new UserAgenceDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPhoneNumber(),

                    agency != null ? agency.getId() : null,  // Handle if user has no agency
                    agency != null ? agency.getName() : null,
                    agency != null ? agency.getContactEmail() : null,
                    agency != null ? agency.getAgencyCode() : null,  // Ensure this is the correct agency code
                    agency != null ? agency.getCity() : null,        // Correctly map city
                    agency != null ? agency.getRegion() : null,      // Correctly map region
                    agency != null ? agency.getContactPhoneNumber() : null
            );
        }).collect(Collectors.toList());
    }

    @Override
    public MessageResponse deleteAgency(Long id, Long userId) {
        logger.info("Deleting agency with id: {}", id);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUser.getUsername());
            throw new AccessDeniedException("Only Admins can delete agencies.");
        }

        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));

        if (!agency.getBank().getId().equals(currentUser.getBank().getId())) {
            logger.error("Access denied: Admin {} is trying to delete an agency not under their bank", currentUser.getUsername());
            throw new AccessDeniedException("You can only delete agencies under your bank.");
        }

        agencyRepository.deleteById(id);
        logger.info("Agency with id {} deleted successfully by Admin {}", id, currentUser.getUsername());
        return new MessageResponse("Agency deleted successfully!", 200);
    }

    @Override
    public Agency getAgencyById(Long agencyId, Long userId) {
        logger.info("Fetching agency with id: {}", agencyId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUser.getUsername());
            throw new AccessDeniedException("Only Admins can view agencies.");
        }


        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", agencyId));
        if (!agency.getBank().getId().equals(currentUser.getBank().getId())) {
            logger.error("Access denied: Admin {} is trying to access an agency not under their bank", currentUser.getUsername());
            throw new AccessDeniedException("You can only view agencies under your bank.");
        }

        return agency;
    }
    @Override
    public Agency getAgencyByIdforall(Long agencyId) {
        logger.info("Fetching agency with id: {}", agencyId);


        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", agencyId));
        return agency;
    }


    @Override
    public MessageResponse updateAgency(Long agencyId, AgencyRequest agencyRequest, Long userId) {
        logger.info("Updating agency with id: {}", agencyId);

        // Find the current user by userId
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if the user has admin role
        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUser.getUsername());
            throw new AccessDeniedException("Only Admins can update agencies.");
        }

        // Find the agency by its ID
        Agency agency = agencyRepository.findById(agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", agencyId));

        // Check if the agency belongs to the current user's bank
        if (!agency.getBank().getId().equals(currentUser.getBank().getId())) {
            logger.error("Access denied: Admin {} is trying to update an agency not under their bank", currentUser.getUsername());
            throw new AccessDeniedException("You can only update agencies under your bank.");
        }

        // Update the agency fields
        agency.setAgencyCode(agencyRequest.getAgencyCode());
        agency.setName(agencyRequest.getName());
        agency.setContactEmail(agencyRequest.getContactEmail());
        agency.setContactPhoneNumber(agencyRequest.getContactPhoneNumber());
        agency.setAdresse(agencyRequest.getAdresse());
        agency.setCity(agencyRequest.getCity());
        agency.setRegion(agencyRequest.getRegion());


        // Save the updated agency
        agencyRepository.save(agency);

        logger.info("Agency {} updated successfully by Admin {}", agency.getName(), currentUser.getUsername());
        return new MessageResponse("Agency updated successfully!", 200);
    }
}
