package com.monetique.PinSenderV0.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monetique.PinSenderV0.Exception.ResourceAlreadyExistsException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.IbankService;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.payload.request.BankRequest;
import com.monetique.PinSenderV0.payload.response.BankListResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    private static final Logger logger = LoggerFactory.getLogger(BankController.class);

    @Autowired
    private HttpServletRequest request;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private IbankService bankservice;

    // Create a new Bank (Only for Super Admin)
    @PostMapping(path = "/Addbanks")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addBank(
            @RequestParam("bankRequest") String bankRequestJson, // Accepting JSON string
            @RequestParam("logo") MultipartFile logo) {

            ObjectMapper objectMapper = new ObjectMapper();
            BankRequest bankRequest;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        try {
            bankRequest = objectMapper.readValue(bankRequestJson, BankRequest.class);
            byte[] logoBytes = logo.getBytes();
            // Pass bankRequest and the logo file to the service
            bankservice.createBank(bankRequest, logoBytes);
            logger.info("Bank {} created successfully by user {}", bankRequest.getName(), currentUserDetails.getUsername());
            return ResponseEntity.ok(new MessageResponse("Bank created successfully!", 200));
        } catch (ResourceAlreadyExistsException e) {
            logger.error("Error creating Bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse(e.getMessage(), 409));
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse("Access denied", 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error Something isn't right error message: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse("Error Something isn't right error message", 404));
        } catch (Exception e) {
            logger.error("Error while creating bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating bank!", 500));
        }


    }


    // List all banks (Accessible to Super Admin)

    @GetMapping(value = "/banks/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> listAllBanks() {
        logger.info("Received request to get banks list");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        try {

            List<TabBank> banks = bankservice.listAllBanks();// Log the success message
           // Create the response
            BankListResponse response = new BankListResponse("Banks retrieved successfully!", 200, banks);// Return the response with the list of banks and the message
            logger.info("List of banks retrieved successfully by user {}", currentUserDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            // Log and return an access denied message
            logger.error("Access Denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Access Denied", 403));
        } catch (Exception e) {
            // Log and return a generic error message
            logger.error("Error while listing banks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving bank list!", 500));
        }
    }
    @GetMapping("/banks/list1")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> listAllBanks1() {
        try {
            // Logic to get the list of banks
            return ResponseEntity.ok(bankservice.listAllBanks());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Access denied", 403));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("An error occurred", 500));
        }
    }

    @GetMapping("banks/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getBankById(@PathVariable Long id) {
        logger.info("Received request to get bank");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        try {
            TabBank bank = bankservice.getBankById(id);
            logger.info(" get bank is successfully by user {}", currentUserDetails.getUsername());
            return ResponseEntity.ok(bank);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse("Access denied", 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error fetching bank: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse("Error fetching bank", 404));
        } catch (Exception e) {
            logger.error("Error while fetching bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error getting bank!", 500));
        }
    }

    // Delete a Bank (Only for Super Admin)
    @DeleteMapping("/banks/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteBank(@PathVariable Long id) {
        logger.info("Received request to delete bank");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }
        try {

            bankservice.deleteBank(id);
            logger.info("Bank with id {} deleted successfully by user {}", id, currentUserDetails.getUsername());

            return ResponseEntity.ok(new MessageResponse("Bank deleted successfully!", 200));
        } catch (ResourceNotFoundException e) {
            logger.error("Bank not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Bank not found", 404));
        } catch (AccessDeniedException e) {
            logger.error("Access Denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResponse("Access Denied", 403));
        } catch (Exception e) {
            logger.error("Error while deleting bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting bank!", 500));
        }
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updatebank(@PathVariable Long id, @RequestParam("bankRequest") String bankRequestJson, // Accepting JSON string
                                        @RequestParam("logo") MultipartFile logo) {

        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("Received request to update bank with id: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        BankRequest bankRequest;

        try {
            bankRequest = objectMapper.readValue(bankRequestJson, BankRequest.class);
            byte[] logoBytes = logo.getBytes();
            MessageResponse response = bankservice.updateBank(id, bankRequest,logoBytes);

            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse("Access denied", 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error updating bank: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse("Access denied", 404));
        }catch (Exception e) {
            logger.error("Error while updating bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating bank!", 500));
        }
    }

    @PostMapping("/add")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> addBank1(
            @RequestParam("bankRequest") String bankRequestJson, // Accepting JSON string
            @RequestParam(value = "logo", required = false) MultipartFile logo) {
        ObjectMapper objectMapper = new ObjectMapper();
        // Get authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("User is not authenticated!", 401));
        }

        try {
            // Convert JSON string to BankRequest object
            BankRequest bankRequest = objectMapper.readValue(bankRequestJson, BankRequest.class);
            byte[] logoBytes = (logo != null && !logo.isEmpty()) ? logo.getBytes() : null;

            // Pass bankRequest and the logo file to the service
            bankservice.createBanklogoinfile(bankRequest, logoBytes);

            logger.info("Bank '{}' created successfully by user '{}'", bankRequest.getName(), authentication.getName());
            return ResponseEntity.ok(new MessageResponse("Bank created successfully!", 200));
        } catch (ResourceAlreadyExistsException e) {
            logger.error("Bank creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponse(e.getMessage(), 409));
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied", 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Resource not found: " + e.getMessage(), 404));
        } catch (IOException e) {
            logger.error("Error processing logo file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Invalid bankRequest data or error reading logo file", 400));
        } catch (Exception e) {
            logger.error("Unexpected error while creating bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error creating bank", 500));
        }
    }

}
