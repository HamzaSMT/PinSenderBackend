package com.monetique.PinSenderV0.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCardholderRequest {

    private String cardNumber;
    private String finalDate;
    private String nationalId;
    private String gsm;


}
