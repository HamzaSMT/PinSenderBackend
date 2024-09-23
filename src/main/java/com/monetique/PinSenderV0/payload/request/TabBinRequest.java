package com.monetique.PinSenderV0.payload.request;

import lombok.Data;

@Data
public class TabBinRequest {

    private String bin;
    private String bankCode;
    private String systemCode;
    private String cardType;
    private String serviceCode;

    // Getters and Setters
}
