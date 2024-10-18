package com.monetique.PinSenderV0.Interfaces;


import com.monetique.PinSenderV0.models.Banks.TabBin;
import com.monetique.PinSenderV0.payload.request.TabBinRequest;
import com.monetique.PinSenderV0.payload.response.BinDTOresponse;

import java.util.List;
import java.util.Optional;

public interface ItabBinService {

    TabBin createTabBin(TabBinRequest tabBinRequest);


    List<BinDTOresponse> getAllTabBins();

    BinDTOresponse getTabBinByBin(String bin);

    TabBin updateTabBin(String bin, TabBinRequest tabBinRequest);

    TabBin getbinbybinnumber(String binNumber);

    void deleteTabBin(String bin);
}