package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.payload.response.WebSocketResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyClient(String cardNumber, String message,int status) {
        messagingTemplate.convertAndSend("/topic/verification-status", new WebSocketResponse(cardNumber, message,status));
    }
}

