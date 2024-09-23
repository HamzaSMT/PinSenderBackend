package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.BankAgencyService;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.payload.request.BankRequest;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BankAgencyServiceImpl implements BankAgencyService {

    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private AgencyRepository agencyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void createBank(BankRequest bankRequest) throws AccessDeniedException {
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
        bankRepository.save(bank);
    }

    @Override
    public List<TabBank> listAllBanks() {
        return bankRepository.findAll();
    }

    @Override
    public TabBank getBankById(Long id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", id));
    }

    @Override
    public TabBank updateBank(Long id, BankRequest bankRequest) {
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

        return bankRepository.save(bank);
    }

    @Override
    public void deleteBank(Long id) {
        // Find the bank to delete
        TabBank bank = getBankById(id);
        bankRepository.delete(bank);
    }


}