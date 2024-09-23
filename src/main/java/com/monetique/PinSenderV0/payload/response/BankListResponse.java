package com.monetique.PinSenderV0.payload.response;

import com.monetique.PinSenderV0.models.Banks.TabBank;
import lombok.Data;

import java.util.List;
@Data
public class BankListResponse {



    private String message;
    private int statusCode;
    private List<TabBank> banks;

    public BankListResponse(String message, int statusCode, List<TabBank> banks) {
        this.message = message;
        this.statusCode = statusCode;
        this.banks = banks;
    }

}
