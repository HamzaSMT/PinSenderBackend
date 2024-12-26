package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SMSResponse {
    private String status;
    private String message;
    private String otp;  // The OTP, if successfully generated
    private int httpStatus;
}
