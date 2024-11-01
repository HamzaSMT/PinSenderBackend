package com.monetique.PinSenderV0.Services.managementbank;

import com.monetique.PinSenderV0.Exception.ResourceAlreadyExistsException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Interfaces.ItabBinService;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Banks.TabBin;
import com.monetique.PinSenderV0.payload.request.TabBinRequest;
import com.monetique.PinSenderV0.payload.response.BinDTOresponse;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.TabBinRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TabBinService implements ItabBinService {

    private static final Logger logger = LoggerFactory.getLogger(BankService.class);

    @Autowired
    private TabBinRepository tabBinRepository;
    @Autowired
    private BankRepository bankRepository;

    @Override

        public TabBin createTabBin(TabBinRequest tabBinRequest) {
            // Check if the bin already exists
            if (tabBinRepository.existsTabBinByBin(tabBinRequest.getBin())) {
                throw new ResourceAlreadyExistsException("TabBin with bin " + tabBinRequest.getBin() + " already exists.");
            }
        TabBank bank = bankRepository.findById(tabBinRequest.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException("TabBank", "id", tabBinRequest.getBankId()));

        TabBin tabBin = new TabBin();
            // Set attributes from request
            tabBin.setBin(tabBinRequest.getBin());
            tabBin.setBank(bank);
            tabBin.setSystemCode(tabBinRequest.getSystemCode());
            tabBin.setCardType(tabBinRequest.getCardType());
            tabBin.setServiceCode(tabBinRequest.getServiceCode());
            tabBin.setKeyDataA(tabBinRequest.getKeyDataA());
            tabBin.setKeyDataB(tabBinRequest.getKeyDataB());
            return tabBinRepository.save(tabBin);
        }

    @Override
    public List<BinDTOresponse> getAllTabBins() {
        List<TabBin> bins = tabBinRepository.findAll();

        if (bins.isEmpty()) {
            throw new ResourceNotFoundException("No bins found");
        }

        return bins.stream()
                .map(BinDTOresponse::new)
                .collect(Collectors.toList());
    }
    @Override
    public BinDTOresponse getTabBinByBin(String bin) {
        Optional<TabBin> tabBinOptional = tabBinRepository.findByBin(bin);

        if (tabBinOptional.isEmpty()) {
            throw new ResourceNotFoundException("Bin not found with bin number: " + bin);
        }

        return new BinDTOresponse(tabBinOptional.get());
    }

    @Override
    public TabBin updateTabBin(String bin, TabBinRequest tabBinRequest) {

        // Fetch the existing TabBin
        TabBin tabBin = tabBinRepository.findById(bin)
                .orElseThrow(() -> new ResourceNotFoundException("TabBin", "bin", bin));
        // Check if the new bin number already exists and it's not the current one

        if (!tabBin.getBin().equals(tabBinRequest.getBin()) && tabBinRepository.existsTabBinByBin(tabBinRequest.getBin())) {
            throw new ResourceAlreadyExistsException("TabBin with bin " + tabBinRequest.getBin() + " already exists.");
        }

        tabBin.setBin(tabBinRequest.getBin());
        tabBin.setSystemCode(tabBinRequest.getSystemCode());
        tabBin.setCardType(tabBinRequest.getCardType());
        tabBin.setServiceCode(tabBinRequest.getServiceCode());
        tabBin.setKeyDataA(tabBinRequest.getKeyDataA());
        tabBin.setKeyDataB(tabBinRequest.getKeyDataB());

        // Save the updated TabBin
        return tabBinRepository.save(tabBin);
    }




    @Override
    public TabBin getbinbybinnumber(String binNumber){
        logger.info("Fetching bin with binNumber: {}", binNumber);
        TabBin bin =  tabBinRepository.findByBin(binNumber)
                .orElseThrow(() -> new ResourceNotFoundException("bin", "binNumber", binNumber));

        return bin;
    }

    @Override
    public void deleteTabBin(String bin) {
        tabBinRepository.deleteById(bin);
    }
}
