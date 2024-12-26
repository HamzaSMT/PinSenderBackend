package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.SMSResponse;

public interface IOtpService {
    // Method to send OTP to the provided phone number
    SMSResponse sendOtp(VerifyCardholderRequest request);

    // Method to validate the OTP input by the user
  //  boolean validateOtp(String phoneNumber, String otp);

    boolean validateOtp(OtpValidationRequest otpValidationRequest);

    // Method to resend OTP
    String resendOtp(String phoneNumber);

    // Method to check if OTP is expired
    boolean isOtpExpired(String phoneNumber);
}
