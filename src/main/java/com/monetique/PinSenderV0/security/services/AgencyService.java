package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.Iagencyservices;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.AgencyRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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
        agency.setBank(currentUser.getBank());

        agencyRepository.save(agency);

        logger.info("Agency {} created successfully by Admin {}", agencyRequest.getAgencyCode(), currentUser.getUsername());
        return new MessageResponse("Agency created successfully!", 200);
    }

    @Override
    public List<Agency> listAllAgencies(Long userId) {
        logger.info("Listing all agencies for user id: {}", userId);

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUser.getUsername());
            throw new AccessDeniedException("Only Admins can list their Agencies.");
        }

        return agencyRepository.findByBankId(currentUser.getBank().getId());
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
        agency.setName(agencyRequest.getName());
        agency.setContactEmail(agencyRequest.getContactEmail());
        agency.setContactPhoneNumber(agencyRequest.getContactPhoneNumber());
        agency.setAgencyCode(agencyRequest.getAgencyCode());

        // Save the updated agency
        agencyRepository.save(agency);

        logger.info("Agency {} updated successfully by Admin {}", agency.getName(), currentUser.getUsername());
        return new MessageResponse("Agency updated successfully!", 200);
    }
}