package com.monetique.PinSenderV0.payload.request;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardHolderDTO {

    private String clientNumber;
    private String cardNumber;
    private String name;
    private String binNumber;
    private String gsm;
    private String email;
    private String bankCode;
    private String pinOffset;
    private String companyName;
    private String agencyCode;
    private String rib;
    private String finalDate;
    private String cardType;
    private String countryCode;
    private String nationalId;

}
