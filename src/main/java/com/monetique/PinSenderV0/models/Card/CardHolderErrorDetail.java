package com.monetique.PinSenderV0.models.Card;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Embeddable
public class CardHolderErrorDetail {

    private String cardNumber;  // Card number associated with the error
    private String errorMessage;  // Error message

    public CardHolderErrorDetail() {}

    public CardHolderErrorDetail(String cardNumber, String errorMessage) {
        this.cardNumber = cardNumber;
        this.errorMessage = errorMessage;
    }


}
