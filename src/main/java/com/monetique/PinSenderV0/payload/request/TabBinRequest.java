package com.monetique.PinSenderV0.payload.request;

import lombok.Data;

@Data
public class TabBinRequest {

    private String bin;
    private Long bankId;
    private String systemCode;
    private String cardType;
    private String serviceCode;
    private String keyDataA;
    private String keyDataB;
    // Getters and Setters
}
