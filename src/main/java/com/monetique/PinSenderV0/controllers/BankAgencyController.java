package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.BankAgencyService;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.BankRequest;
import com.monetique.PinSenderV0.payload.response.BankListResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Autowired
    private BankAgencyService bankAgencyService;

    // Create a new Bank (Only for Super Admin)
    @PostMapping("/Addbanks")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createBank(@RequestBody BankRequest bankRequest) {
        logger.info("Received request to create bank: {}", bankRequest);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can create Banks.");
        }

        bankAgencyService.createBank(bankRequest);

        logger.info("Bank {} created successfully by user {}", bankRequest.getName(), currentUserDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Bank created successfully!", 200));
    }

    // List all banks (Accessible to Super Admin)

    @GetMapping(value = "/banks/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> listAllBanks() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Retrieve the list of banks
            List<TabBank> banks = bankAgencyService.listAllBanks();

            // Log the success message
            logger.info("List of banks retrieved successfully by user {}", currentUserDetails.getUsername());

            // Create the response
            BankListResponse response = new BankListResponse("Banks retrieved successfully!", 200, banks);

            // Return the response with the list of banks and the message
            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            // Log and return an access denied message
            logger.error("Access Denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage(), 403));
        } catch (Exception e) {
            // Log and return a generic error message
            logger.error("Error while listing banks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving bank list!", 500));
        }
    }

    @GetMapping("banks/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<TabBank> getBankById(@PathVariable Long id) {
        logger.info("Received request to create bank");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can create Banks.");
        }

        TabBank bank = bankAgencyService.getBankById(id);
        return ResponseEntity.ok(bank);
    }

    // Delete a Bank (Only for Super Admin)
    @DeleteMapping("/banks/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteBank(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

            bankAgencyService.deleteBank(id);
            logger.info("Bank with id {} deleted successfully by user {}", id, currentUserDetails.getUsername());

            return ResponseEntity.ok(new MessageResponse("Bank deleted successfully!", 200));
        } catch (ResourceNotFoundException e) {
            logger.error("Bank not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (AccessDeniedException e) {
            logger.error("Access Denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage(), 403));
        } catch (Exception e) {
            logger.error("Error while deleting bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting bank!", 500));
        }
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
    /*
    //////////////////////////
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createAgency(@RequestBody @Valid AgencyRequest agencyRequest) {
        logger.info("Received request to create agency: {}", agencyRequest);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can create Agencies.");
        }

        agencyService.createAgency(agencyRequest);

        logger.info("Agency {} created successfully by user {}", agencyRequest.getName(), currentUserDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Agency created successfully!", 200));
    }

    // Get a list of all agencies (Accessible to Super Admin)
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> listAllAgencies() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

            List<Agency> agencies = agencyService.listAllAgencies();

            logger.info("List of agencies retrieved successfully by user {}", currentUserDetails.getUsername());
            return ResponseEntity.ok(new AgencyListResponse("Agencies retrieved successfully!", 200, agencies));

        } catch (AccessDeniedException e) {
            logger.error("Access Denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse(e.getMessage(), 403));
        } catch (Exception e) {
            logger.error("Error while listing agencies: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving agency list!", 500));
        }
    }

    // Get agency by ID (Accessible to Super Admin)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getAgencyById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

            Agency agency = agencyService.getAgencyById(id);
            logger.info("Agency with ID {} retrieved by user {}", id, currentUserDetails.getUsername());
            return ResponseEntity.ok(agency);

        } catch (ResourceNotFoundException e) {
            logger.error("Agency not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error while retrieving agency: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving agency!", 500));
        }
    }

    // Update an existing agency (Accessible to Super Admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateAgency(@PathVariable Long id, @RequestBody @Valid AgencyRequest agencyRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

            agencyService.updateAgency(id, agencyRequest);
            logger.info("Agency with ID {} updated successfully by user {}", id, currentUserDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Agency updated successfully!", 200));

        } catch (ResourceNotFoundException e) {
            logger.error("Agency not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error while updating agency: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating agency!", 500));
        }
    }

    // Delete an agency (Accessible to Super Admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteAgency(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

            agencyService.deleteAgency(id);
            logger.info("Agency with ID {} deleted successfully by user {}", id, currentUserDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Agency deleted successfully!", 200));

        } catch (ResourceNotFoundException e) {
            logger.error("Agency not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error while deleting agency: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting agency!", 500));
        }
    }
*/

}
