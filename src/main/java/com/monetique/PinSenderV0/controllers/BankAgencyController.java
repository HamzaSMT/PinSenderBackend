package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Agency;
import com.monetique.PinSenderV0.models.Bank;
import com.monetique.PinSenderV0.models.User;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bankagency")
public class BankAgencyController {

    private static final Logger logger = LoggerFactory.getLogger(BankAgencyController.class);

    @Autowired
    BankRepository bankRepository;

    @Autowired
    AgencyRepository agencyRepository;

    @Autowired
    UserRepository userRepository;

    // Create a new Bank (Only for Super Admin)
    @PostMapping("/Addbanks")
    public ResponseEntity<?> createBank(@RequestParam String bankName) {
        logger.info("Received request to create bank: {}", bankName);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can create Banks.");
        }

        Bank bank = new Bank(bankName);  // Now this constructor works
        bankRepository.save(bank);

        logger.info("Bank {} created successfully by user {}", bankName, currentUserDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Bank created successfully!", 200));
    }



    // List all banks (Accessible to Super Admin)

    @GetMapping("/Listbanks")
    public ResponseEntity<?> listAllBanks() {
        logger.info("Received request to list all banks");

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Find the current authenticated user from the database
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        // Check if the user has the SUPER_ADMIN role
        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a SUPER_ADMIN", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only SUPER_ADMINs can access this endpoint.");
        }

        List<Bank> banks = bankRepository.findAll();
        return ResponseEntity.ok(banks);
    }

    // Create a new Agency (Only for Admins)
    @PostMapping("/Addagencies")
    public ResponseEntity<?> createAgency(@RequestParam String agencyName) {
        logger.info("Received request to create agency: {}", agencyName);

        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Find the current authenticated user from the database
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        // Ensure the current user is an Admin
        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Admins can create Agencies.");
        }

        // Ensure the Admin is associated with a bank
        if (currentUser.getBank() == null) {
            logger.error("Admin {} is not associated with a bank, cannot create an agency", currentUserDetails.getUsername());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Admin is not associated with any bank.", 400));
        }

        // Create the agency and associate it with the Admin's bank
        Agency agency = new Agency();
        agency.setName(agencyName);
        agency.setBank(currentUser.getBank());  // Associate the agency with the Admin's bank
        agencyRepository.save(agency);

        logger.info("Agency {} created successfully by Admin {}", agencyName, currentUserDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Agency created successfully!", 200));
    }

    // List all Agencies for the Admin's Bank (Accessible to Admins)
    @GetMapping("/Listagencies")
    public ResponseEntity<?> listAllAgencies() {
        logger.info("Received request to list all agencies");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Admins can list their Agencies.");
        }

        List<Agency> agencies = agencyRepository.findByBankId(currentUser.getBank().getId());
        return ResponseEntity.ok(agencies);
    }

    // Delete a Bank (Only for Super Admin)
    @DeleteMapping("/banks/{id}")
    public ResponseEntity<?> deleteBank(@PathVariable Long id) {
        logger.info("Received request to delete bank with id: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can delete Banks.");
        }

        Bank bank = bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));

        bankRepository.deleteById(id);
        logger.info("Bank with id {} deleted successfully", id);
        return ResponseEntity.ok(new MessageResponse("Bank deleted successfully!", 200));
    }

    // Delete an Agency (Only for Admin)
    @DeleteMapping("/agencies/{id}")
    public ResponseEntity<?> deleteAgency(@PathVariable Long id) {
        logger.info("Received request to delete agency with id: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            logger.error("Access denied: User {} is not an Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Admins can delete Agencies.");
        }

        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", id));

        if (!agency.getBank().getId().equals(currentUser.getBank().getId())) {
            logger.error("Access denied: Admin {} is trying to delete an agency not under their bank", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: You can only delete agencies under your bank.");
        }

        agencyRepository.deleteById(id);
        logger.info("Agency with id {} deleted successfully by Admin {}", id, currentUserDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Agency deleted successfully!", 200));
    }
}
