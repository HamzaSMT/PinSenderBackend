package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.IbankService;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.BankRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class BankService implements IbankService {

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Override
    public MessageResponse createBank(BankRequest bankRequest, MultipartFile logoFile) throws AccessDeniedException {
        logger.info("Creating bank with name: {}", bankRequest.getName());

        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException("Error: Only Super Admin can create Banks.");
        }

        TabBank bank = new TabBank();
        bank.setName(bankRequest.getName());
        bank.setCodeBanque(bankRequest.getCodeBanque());
        bank.setLibelleBanque(bankRequest.getLibelleBanque());
        bank.setEnseigneBanque(bankRequest.getEnseigneBanque());
        bank.setIca(bankRequest.getIca());
        bank.setBinAcquereurVisa(bankRequest.getBinAcquereurVisa());
        bank.setBinAcquereurMcd(bankRequest.getBinAcquereurMcd());
        bank.setCtb(bankRequest.getCtb());
        bank.setBanqueEtrangere(bankRequest.getBanqueEtrangere());

        // Handle logo upload if provided
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                byte[] logoBytes = logoFile.getBytes();  // Convert MultipartFile to byte array
                bank.setLogo(logoBytes);  // Set logo to the bank entity
            } catch (IOException e) {
                logger.error("Error uploading logo: {}", e.getMessage());
                throw new RuntimeException("Failed to upload logo", e);
            }

        }

        // Save the bank entity
        bankRepository.save(bank);

        logger.info("Bank {} created successfully by Admin {}", bankRequest.getName(), currentUser.getUsername());
        return new MessageResponse("Bank created successfully!", 200);
    }


    @Override
    public List<TabBank> listAllBanks() {
        logger.info("Listing all banks ");
        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException("Error: Only Super Admin can get Banks.");
        }


        return bankRepository.findAll();
    }

    @Override
    public TabBank getBankById(Long id) {
        logger.info("Fetching agency with id: {}", id);
        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException("Error: Only Super Admin can get Bank.");
        }

      TabBank bank =  bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));

        return bank;
    }

    @Override
    public MessageResponse updateBank(Long id, BankRequest bankRequest) {
        logger.info("Updating bank with id: {}", id);
        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException("Error: Only Super Admin can update Banks.");
        }
        // Find the bank to update
        TabBank bank = getBankById(id);

        // Update bank details
        bank.setName(bankRequest.getName());
        bank.setCodeBanque(bankRequest.getCodeBanque());
        bank.setLibelleBanque(bankRequest.getLibelleBanque());
        bank.setEnseigneBanque(bankRequest.getEnseigneBanque());
        bank.setIca(bankRequest.getIca());
        bank.setBinAcquereurVisa(bankRequest.getBinAcquereurVisa());
        bank.setBinAcquereurMcd(bankRequest.getBinAcquereurMcd());
        bank.setCtb(bankRequest.getCtb());
        bank.setBanqueEtrangere(bankRequest.getBanqueEtrangere());

        bankRepository.save(bank);

        logger.info("bank {} updated successfully by Admin {}", bank.getName(), currentUser.getUsername());
        return new MessageResponse("Agency updated successfully!", 200);

    }

    @Override
    public MessageResponse deleteBank(Long id) {
        logger.info("Deleting bank with id: {}", id);
        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException("Error: Only Super Admin can delete Banks.");
        }


        // Find the bank to delete
        TabBank bank = getBankById(id);
        bankRepository.delete(bank);
        logger.info("Bank with id {} deleted successfully by Admin {}", id, currentUser.getUsername());
        return new MessageResponse("Bank deleted successfully!", 200);
    }

}