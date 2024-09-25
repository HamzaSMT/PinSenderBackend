package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.ResourceAlreadyExistsException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.ItabBinService;
import com.monetique.PinSenderV0.models.Banks.TabBin;
import com.monetique.PinSenderV0.payload.request.TabBinRequest;
import com.monetique.PinSenderV0.repository.TabBinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TabBinService implements ItabBinService {

    @Autowired
    private TabBinRepository tabBinRepository;

    @Override

        public TabBin createTabBin(TabBinRequest tabBinRequest) {
            // Check if the bin already exists
            if (tabBinRepository.existsTabBinByBin(tabBinRequest.getBin())) {
                throw new ResourceAlreadyExistsException("TabBin with bin " + tabBinRequest.getBin() + " already exists.");
            }
            TabBin tabBin = new TabBin();
            // Set attributes from request
            tabBin.setBin(tabBinRequest.getBin());
            tabBin.setBankCode(tabBinRequest.getBankCode());
            tabBin.setSystemCode(tabBinRequest.getSystemCode());
            tabBin.setCardType(tabBinRequest.getCardType());
            tabBin.setServiceCode(tabBinRequest.getServiceCode());

            return tabBinRepository.save(tabBin);
        }

    @Override
    public Optional<TabBin> getTabBinByBin(String bin) {
        return tabBinRepository.findById(bin);
    }

    @Override
    public List<TabBin> getAllTabBins() {
        return tabBinRepository.findAll();
    }

    @Override
    public TabBin updateTabBin(String bin, TabBinRequest tabBinRequest) {
        // Check if the new bin already exists
        if (tabBinRepository.existsTabBinByBin(tabBinRequest.getBin())) {
            throw new ResourceAlreadyExistsException("TabBin with bin " + tabBinRequest.getBin() + " already exists.");
        }

        // Fetch the existing TabBin
        TabBin tabBin = tabBinRepository.findById(bin)
                .orElseThrow(() -> new ResourceNotFoundException("TabBin", "bin", bin));

        // Update the fields
        tabBin.setBin(tabBinRequest.getBin());
        tabBin.setBankCode(tabBinRequest.getBankCode());
        tabBin.setSystemCode(tabBinRequest.getSystemCode());
        tabBin.setCardType(tabBinRequest.getCardType());
        tabBin.setServiceCode(tabBinRequest.getServiceCode());

        // Save the updated TabBin
        return tabBinRepository.save(tabBin);
    }

    @Override
    public void deleteTabBin(String bin) {
        tabBinRepository.deleteById(bin);
    }
}
