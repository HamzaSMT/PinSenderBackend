package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.payload.request.OtpValidationRequest;
import com.monetique.PinSenderV0.payload.request.VerifyCardholderRequest;
import com.monetique.PinSenderV0.payload.response.OtpResendResult;
import com.monetique.PinSenderV0.payload.response.OtpValidationResult;
import com.monetique.PinSenderV0.payload.response.SMSResponse;

public interface IOtpService {
    // Method to send OTP to the provided phone number
    SMSResponse sendOtp(VerifyCardholderRequest request);

    // Method to validate the OTP input by the user
  //  boolean validateOtp(String phoneNumber, String otp);

    OtpValidationResult validateOtp(OtpValidationRequest otpValidationRequest) throws Exception;

    // Method to resend OTP
    OtpResendResult resendOtp(String phoneNumber);

}
