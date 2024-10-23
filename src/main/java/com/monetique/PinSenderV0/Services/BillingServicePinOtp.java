package com.monetique.PinSenderV0.Services;

import com.monetique.PinSenderV0.models.Banks.SentitmePinOTP;
import com.monetique.PinSenderV0.repository.SentItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillingServicePinOtp {

    @Autowired
    private SentItemRepository sentItemRepository;

    // Log the sent item (OTP or PIN)
    public void logSentItem(Long agentId, Long branchId, Long bankId, String type) {
        SentitmePinOTP sentItem = new SentitmePinOTP();
        sentItem.setAgentId(agentId);
        sentItem.setBranchId(branchId);
        sentItem.setBankId(bankId);
        sentItem.setType(type);
        sentItemRepository.save(sentItem);
    }

    // Calculate the number of OTPs or PINs sent by agent
    public Long getOTPsSentByAgent(Long agentId) {
        return sentItemRepository.countByAgentIdAndType(agentId, "OTP");
    }

    // Calculate the number of PINs sent by agent
    public Long getPINsSentByAgent(Long agentId) {
        return sentItemRepository.countByAgentIdAndType(agentId, "PIN");
    }

    // Calculate the number of OTPs sent by branch
    public Long getOTPsSentByBranch(Long branchId) {
        return sentItemRepository.countByBranchIdAndType(branchId, "OTP");
    }

    // Calculate the number of PINs sent by branch
    public Long getPINsSentByBranch(Long branchId) {
        return sentItemRepository.countByBranchIdAndType(branchId, "PIN");
    }

    // Calculate the number of OTPs sent by bank
    public Long getOTPsSentByBank(Long bankId) {
        return sentItemRepository.countByBankIdAndType(bankId, "OTP");
    }

    // Calculate the number of PINs sent by bank
    public Long getPINsSentByBank(Long bankId) {
        return sentItemRepository.countByBankIdAndType(bankId, "PIN");
    }
}

