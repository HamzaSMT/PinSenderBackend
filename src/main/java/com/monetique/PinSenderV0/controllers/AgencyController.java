package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.payload.request.AgencyRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.security.services.AgencyService;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agency")
public class AgencyController {

    private static final Logger logger = LoggerFactory.getLogger(AgencyController.class);

    @Autowired
    private AgencyService agencyService;

    // Create Agency
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createAgency(@RequestBody AgencyRequest agencyRequest) {
        logger.info("Received request to create agency: {}", agencyRequest.getName());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            MessageResponse response = agencyService.createAgency(agencyRequest, currentUserDetails.getId());
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error creating agency: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage(), 404));
        }catch (Exception e) {
            logger.error("Error while deleting bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating branch!", 500));
        }
    }

    // List all Agencies
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> listAllAgencies() {
        logger.info("Received request to list all agencies");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            List<Agency> agencies = agencyService.listAllAgencies(currentUserDetails.getId());
            return ResponseEntity.ok(agencies);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error listing agencies: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage(), 404));
        }catch (Exception e) {
            logger.error("Error while getting agencys: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error getting agencys!", 500));
        }
    }

    // Delete Agency
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteAgency(@PathVariable Long id) {
        logger.info("Received request to delete agency with id: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            MessageResponse response = agencyService.deleteAgency(id, currentUserDetails.getId());
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error deleting agency: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage(), 404));
        }catch (Exception e) {
            logger.error("Error while deleting bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting agency!", 500));
        }
    }

    // Get Agency by ID
    @GetMapping("/get/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAgencyById(@PathVariable Long id) {
        logger.info("Received request to get agency with id: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            Agency agency = agencyService.getAgencyById(id, currentUserDetails.getId());
            return ResponseEntity.ok(agency);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error fetching agency: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage(), 404));
        }catch (Exception e) {
            logger.error("Error while deleting bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error getting agency!", 500));
        }
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateAgency(@PathVariable Long id, @RequestBody AgencyRequest agencyRequest) {
        logger.info("Received request to update agency with id: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();

        try {
            MessageResponse response = agencyService.updateAgency(id, agencyRequest, currentUserDetails.getId());
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(403).body(new MessageResponse(e.getMessage(), 403));
        } catch (ResourceNotFoundException e) {
            logger.error("Error updating agency: {}", e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse(e.getMessage(), 404));
        }catch (Exception e) {
            logger.error("Error while deleting bank: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating agency!", 500));
        }

    }


}