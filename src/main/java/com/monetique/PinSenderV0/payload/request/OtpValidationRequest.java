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

    private String cardNumber;
    private String phoneNumber;
    private String otp;
    private Long agentId;  // Add these fields
    private Long branchId; // Add these fields
    private Long bankId;   // Add these fields


}