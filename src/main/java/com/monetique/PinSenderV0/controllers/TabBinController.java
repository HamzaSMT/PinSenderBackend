package com.monetique.PinSenderV0.controllers;


import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceAlreadyExistsException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.ItabBinService;
import com.monetique.PinSenderV0.models.Banks.TabBin;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.TabBinRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.UserRepository;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/tabbin")
public class TabBinController {

    private static final Logger logger = LoggerFactory.getLogger(TabBinController.class);

    @Autowired
    private ItabBinService tabBinService;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createTabBin(@RequestBody TabBinRequest tabBinRequest) {
        logger.info("Received request to create TabBin: {}", tabBinRequest);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can create Bin.");
        }

        try {
            TabBin createdTabBin = tabBinService.createTabBin(tabBinRequest);
            logger.info("TabBin created successfully with bin: {}", createdTabBin.getBin());
            return ResponseEntity.ok(new MessageResponse("TabBin created successfully!", 200));
        } catch (ResourceAlreadyExistsException e) {
            logger.error("Error creating TabBin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage(), 409));
        }
    }
    @GetMapping("/{bin}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> getTabBinByBin(@PathVariable String bin) {
        logger.info("Fetching TabBin with bin: {}", bin);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can get bin.");
        }


        return tabBinService.getTabBinByBin(bin)
                .map(tabBin -> {
                    logger.info("TabBin found: {}", tabBin);
                    return ResponseEntity.ok(tabBin);
                })
                .orElseGet(() -> {
                    logger.warn("TabBin not found with bin: {}", bin);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTabBins() {
        logger.info("Fetching all TabBins");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can get all bins.");
        }


        List<TabBin> tabBins = tabBinService.getAllTabBins();
        return ResponseEntity.ok(tabBins);
    }

    @PutMapping("/update/{bin}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> updateTabBin(@PathVariable String bin, @RequestBody TabBinRequest tabBinRequest) {
        logger.info("Updating TabBin with bin: {}", bin);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can update bin.");
        }

        try {
            TabBin updatedTabBin = tabBinService.updateTabBin(bin, tabBinRequest);
            logger.info("TabBin updated successfully with bin: {}", updatedTabBin.getBin());
            return ResponseEntity.ok(updatedTabBin);
        } catch (ResourceAlreadyExistsException e) {
            logger.error("Error updating TabBin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage(), 409));
        } catch (ResourceNotFoundException e) {
            logger.error("TabBin not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error while updating TabBin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating TabBin!", 500));
        }
    }

    @DeleteMapping("/delete/{bin}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deleteTabBin(@PathVariable String bin) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userRepository.findById(currentUserDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            logger.error("Access denied: User {} is not a Super Admin", currentUserDetails.getUsername());
            throw new AccessDeniedException("Error: Only Super Admin can delete bin.");
        }

        try {
            tabBinService.deleteTabBin(bin);
            logger.info("TabBin with bin {} deleted successfully", bin);
            return ResponseEntity.ok(new MessageResponse("TabBin deleted successfully!", 200));
        } catch (ResourceNotFoundException e) {
            logger.error("TabBin not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
        } catch (Exception e) {
            logger.error("Error while deleting TabBin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting TabBin!", 500));
        }
    }
}