package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.payload.request.BankRequest;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IbankService {


    MessageResponse createBank(BankRequest bankRequest, byte[] logo) throws AccessDeniedException;

    List<TabBank> listAllBanks();
    TabBank getBankById(Long id);
    MessageResponse updateBank(Long id, BankRequest bankRequest,byte[] logo);
    MessageResponse deleteBank(Long id);


    // List<Bank> listAllBanks();
    // void createAgency(AgencyRequest agencyRequest) throws AccessDeniedException;
    // List<Agency> listAllAgencies();
    // void deleteBank(Long id) throws AccessDeniedException;
    // void deleteAgency(Long id) throws AccessDeniedException;
}