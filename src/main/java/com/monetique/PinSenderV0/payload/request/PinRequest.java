package com.monetique.PinSenderV0.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PinRequest {

    private String pvka;
    private String offset;
    private String pinLength;
    private String right12Pan;
    private String decimTable;
    private String pan10;
}
