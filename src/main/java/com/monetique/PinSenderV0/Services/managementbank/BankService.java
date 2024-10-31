package com.monetique.PinSenderV0.Services.managementbank;

import com.monetique.PinSenderV0.Exception.ResourceAlreadyExistsException;
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

import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class BankService implements IbankService {
    private final String storageDirectory = "logos/";
    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Override
    public MessageResponse createBank(BankRequest bankRequest, byte[] logo) throws AccessDeniedException {

        logger.info("Creating bank with name: {}", bankRequest.getName());

        UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

        if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
            throw new AccessDeniedException("Error: Only Super Admin can create Banks.");
        }
        if (bankRepository.existsTabBankBybankCode(bankRequest.getBankCode())) {
            throw new ResourceAlreadyExistsException("Bank with bank code " + bankRequest.getBankCode()+ " already exists.");
        }
        TabBank bank = new TabBank();
        bank.setName(bankRequest.getName());
        bank.setBankCode(bankRequest.getBankCode());
        bank.setLibelleBanque(bankRequest.getLibelleBanque());
        bank.setEnseigneBanque(bankRequest.getEnseigneBanque());
        bank.setIca(bankRequest.getIca());
        bank.setBinAcquereurVisa(bankRequest.getBinAcquereurVisa());
        bank.setBinAcquereurMcd(bankRequest.getBinAcquereurMcd());
        bank.setCtb(bankRequest.getCtb());
        bank.setBanqueEtrangere(bankRequest.isBanqueEtrangere());

        // Handle logo upload if provided


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
        logger.info("Fetching bank with id: {}", id);
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
    public TabBank getBankByIdforall(Long id) {
        logger.info("Fetching bank with id: {}", id);

        TabBank bank =  bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));

        return bank;
    }

    @Override
    public TabBank getbankbybancode(String bankCode){
        logger.info("Fetching bank with bankCode: {}", bankCode);
        TabBank bank =  bankRepository.findBybankCode(bankCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Bank", "bankCode", bankCode));

        return bank;
    }

    @Override
    public MessageResponse updateBank(Long id, BankRequest bankRequest, byte[] logo) {
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
        bank.setBankCode(bankRequest.getBankCode());
        bank.setLibelleBanque(bankRequest.getLibelleBanque());
        bank.setEnseigneBanque(bankRequest.getEnseigneBanque());
        bank.setIca(bankRequest.getIca());
        bank.setBinAcquereurVisa(bankRequest.getBinAcquereurVisa());
        bank.setBinAcquereurMcd(bankRequest.getBinAcquereurMcd());
        bank.setCtb(bankRequest.getCtb());
        bank.setBanqueEtrangere(bankRequest.isBanqueEtrangere());
        if (logo != null && logo.length > 0) {
            // Save logo file and get the file path
            String logoPath = saveLogoFile(logo, bankRequest.getBankCode());
            bank.setLogoFilePath(logoPath); // Save only the path in the database
        }

        bankRepository.save(bank);

        logger.info("bank {} updated successfully by Admin {}", bank.getName(), currentUser.getUsername());
        return new MessageResponse("bank updated successfully!", 200);

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



    public String saveLogoFile(byte[] logoBytes, String bankCode) {
        try {
            // Ensure the directory exists
            Files.createDirectories(Paths.get(storageDirectory));

            // Generate a unique filename based on the bank code and timestamp
            String fileName = bankCode + "_" + System.currentTimeMillis() + ".png";
            Path filePath = Paths.get(storageDirectory + fileName);

            // Write the bytes to a file
            Files.write(filePath, logoBytes);

            // Return the file path for storage in the database
            return filePath.toString();
        } catch (IOException e) {
            logger.error("Error saving logo file for bank code {}: {}", bankCode, e.getMessage());
            throw new RuntimeException("Failed to save logo file", e);
        }
    }

    public MessageResponse createBanklogoinfile(BankRequest bankRequest, byte[] logo) {
        try {
            logger.info("Creating bank with name: {}", bankRequest.getName());

            UserDetails currentUserDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findByUsername(currentUserDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", currentUserDetails.getUsername()));

            if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().name().equals("ROLE_SUPER_ADMIN"))) {
                throw new AccessDeniedException("Error: Only Super Admin can create Banks.");
            }

            if (bankRepository.existsTabBankBybankCode(bankRequest.getBankCode())) {
                throw new ResourceAlreadyExistsException("Bank with bank code " + bankRequest.getBankCode() + " already exists.");
            }

            TabBank bank = new TabBank();
            bank.setName(bankRequest.getName());
            bank.setBankCode(bankRequest.getBankCode());
            bank.setLibelleBanque(bankRequest.getLibelleBanque());
            bank.setEnseigneBanque(bankRequest.getEnseigneBanque());
            bank.setIca(bankRequest.getIca());
            bank.setBinAcquereurVisa(bankRequest.getBinAcquereurVisa());
            bank.setBinAcquereurMcd(bankRequest.getBinAcquereurMcd());
            bank.setCtb(bankRequest.getCtb());
            bank.setBanqueEtrangere(bankRequest.isBanqueEtrangere());

            // Handle logo upload if provided
            if (logo != null && logo.length > 0) {
                String logoPath = saveLogoFile(logo, bankRequest.getBankCode());
                bank.setLogoFilePath(logoPath); // Save only the path in the database
            }

            // Save the bank entity
            bankRepository.save(bank);

            logger.info("Bank {} created successfully by Admin {}", bankRequest.getName(), currentUser.getUsername());
            return new MessageResponse("Bank created successfully!", 200);

        } catch (AccessDeniedException e) {
            logger.error("Access denied: {}", e.getMessage());
            throw e;
        } catch (ResourceAlreadyExistsException e) {
            logger.error("Bank creation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("An error occurred during bank creation: {}", e.getMessage());
            return new MessageResponse("Failed to create bank: " + e.getMessage(), 500);
        }
    }



}