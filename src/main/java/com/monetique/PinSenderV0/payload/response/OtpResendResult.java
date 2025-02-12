package com.monetique.PinSenderV0.payload.response;

public class OtpResendResult {
    public enum Status {
        SUCCESS,
        NO_EXISTING_OTP,
        TOO_MANY_ATTEMPTS,
        RATE_LIMIT_EXCEEDED,
        NUMBER_BLOCKED,
        ERROR
    }

    private Status status;
    private String message;

    public OtpResendResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

