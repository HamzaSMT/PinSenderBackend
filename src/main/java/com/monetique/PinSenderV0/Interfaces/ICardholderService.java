package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.models.Card.CardHolderLoadReport;
import com.monetique.PinSenderV0.models.Card.TabCardHolder;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.TabCardHolderresponse;

import java.util.List;

public interface ICardholderService {
    List<TabCardHolderresponse> getAllCardHolders();

    TabCardHolder getCardHolderByCardNumber(String cardNumber);

    TabCardHolder extractCardHolderAttributes(String line);

    void updateCardHolder(TabCardHolder existingCardHolder, TabCardHolder updatedCardHolder);

    void processCardHolderLine(String line);



    void processCardHolderLines(List<String> lines, String fileName);

    void verifyCardholder(VerifyCardholderRequest request);

    CardHolderLoadReport getLoadReportById(Long id);

    List<CardHolderLoadReport> getAllLoadReports();
}
