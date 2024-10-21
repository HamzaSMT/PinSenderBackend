package com.monetique.PinSenderV0.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketResponse {
    private String cardNumber;
    private String message;
    private int status;

    public WebSocketResponse(String cardNumber, String message, int status) {
        this.cardNumber = cardNumber;
        this.message = message;
        this.status = 0;
    }
}
