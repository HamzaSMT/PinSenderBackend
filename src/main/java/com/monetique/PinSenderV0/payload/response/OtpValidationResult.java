package com.monetique.PinSenderV0.payload.response;

public class OtpValidationResult {
    private final OtpValidationStatus status;

    public OtpValidationResult(OtpValidationStatus status) {
        this.status = status;
    }

    public OtpValidationStatus getStatus() {
        return status;
    }
}

