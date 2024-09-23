package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.payload.request.BankRequest;

import java.util.List;

public interface BankAgencyService {
    void createBank(BankRequest bankRequest) throws AccessDeniedException;
    List<TabBank> listAllBanks();
    TabBank getBankById(Long id);
    TabBank updateBank(Long id, BankRequest bankRequest);
    void deleteBank(Long id);


    // List<Bank> listAllBanks();
    // void createAgency(AgencyRequest agencyRequest) throws AccessDeniedException;
    // List<Agency> listAllAgencies();
    // void deleteBank(Long id) throws AccessDeniedException;
    // void deleteAgency(Long id) throws AccessDeniedException;
}