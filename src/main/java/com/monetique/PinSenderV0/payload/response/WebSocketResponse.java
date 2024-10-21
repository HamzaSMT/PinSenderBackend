package com.monetique.PinSenderV0.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketResponse {
    private String cardNumber;
    private String message;

    public WebSocketResponse(String cardNumber, String message) {
        this.cardNumber = cardNumber;
        this.message = message;
    }
}
