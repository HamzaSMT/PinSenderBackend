package com.monetique.PinSenderV0.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardholderVerificationRequest {
    private String cardNumber;
    private String cin;
    private String phoneNumber;
    private String expirationDate;
}