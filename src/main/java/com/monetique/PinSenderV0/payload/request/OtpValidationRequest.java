package com.monetique.PinSenderV0.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class
OtpValidationRequest {
    private String phoneNumber;
    private String otp;
}