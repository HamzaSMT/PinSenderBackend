package com.monetique.PinSenderV0.Interfaces;


import com.monetique.PinSenderV0.models.Banks.TabBin;
import com.monetique.PinSenderV0.payload.request.TabBinRequest;

import java.util.List;
import java.util.Optional;

public interface ItabBinService {

    TabBin createTabBin(TabBinRequest tabBinRequest);

    Optional<TabBin> getTabBinByBin(String bin);

    List<TabBin> getAllTabBins();

    TabBin updateTabBin(String bin, TabBinRequest tabBinRequest);

    void deleteTabBin(String bin);
}