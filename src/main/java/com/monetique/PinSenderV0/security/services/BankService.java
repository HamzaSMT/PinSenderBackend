package com.monetique.PinSenderV0.security.services;

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
import java.util.List;

@Service
public class BankService implements IbankService {

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
            throw new ResourceAlreadyExistsException("TabBin with bin " + bankRequest.getBankCode()+ " already exists.");
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
        bank.setLogo(logo);

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
        if (logo != null) {
            bank.setLogo(logo);
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

}