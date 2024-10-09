package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.models.Banks.TabCardHolder;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.TabCardHolderresponse;

import java.util.List;

public interface ICardholderService {
    List<TabCardHolderresponse> getAllCardHolders();

    TabCardHolder extractCardHolderAttributes(String line);

    void processCardHolderLine(String line);

    void createCardHolder(String line);

    void updateCardHolder(TabCardHolder existingCardHolder, String line);

    String processCardHolderLines(List<String> lines);

    void verifyCardholder(VerifyCardholderRequest request);
}
