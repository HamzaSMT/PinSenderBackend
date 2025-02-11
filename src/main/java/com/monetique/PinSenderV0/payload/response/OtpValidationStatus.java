package com.monetique.PinSenderV0.payload.response;

public enum OtpValidationStatus {
    SUCCESS,
    INVALID_OTP,
    OTP_EXPIRED,
    NUMBER_BLOCKED,
    ERROR
}