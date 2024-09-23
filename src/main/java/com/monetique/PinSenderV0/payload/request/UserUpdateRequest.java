package com.monetique.PinSenderV0.payload.request;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String email;
    private String phoneNumber;

  }
