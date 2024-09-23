package com.monetique.PinSenderV0.controllers;


import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.security.services.BillingServicePinOtp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingServicePinOtp billingService;

    // Endpoint for OTPs sent by agent
    @GetMapping("/otps/sent-by-agent/{agentId}")
    public ResponseEntity<?> getOTPsSentByAgent(@PathVariable Long agentId) {
        Long count = billingService.getOTPsSentByAgent(agentId);
        return ResponseEntity.ok(new MessageResponse("OTPs sent by agent: " + count, 200));
    }

    // Endpoint for PINs sent by agent
    @GetMapping("/pins/sent-by-agent/{agentId}")
    public ResponseEntity<?> getPINsSentByAgent(@PathVariable Long agentId) {
        Long count = billingService.getPINsSentByAgent(agentId);
        return ResponseEntity.ok(new MessageResponse("PINs sent by agent: " + count, 200));
    }

    // Endpoint for OTPs sent by branch
    @GetMapping("/otps/sent-by-branch/{branchId}")
    public ResponseEntity<?> getOTPsSentByBranch(@PathVariable Long branchId) {
        Long count = billingService.getOTPsSentByBranch(branchId);
        return ResponseEntity.ok(new MessageResponse("OTPs sent by branch: " + count, 200));
    }

    // Endpoint for PINs sent by branch
    @GetMapping("/pins/sent-by-branch/{branchId}")
    public ResponseEntity<?> getPINsSentByBranch(@PathVariable Long branchId) {
        Long count = billingService.getPINsSentByBranch(branchId);
        return ResponseEntity.ok(new MessageResponse("PINs sent by branch: " + count, 200));
    }

    // Endpoint for OTPs sent by bank
    @GetMapping("/otps/sent-by-bank/{bankId}")
    public ResponseEntity<?> getOTPsSentByBank(@PathVariable Long bankId) {
        Long count = billingService.getOTPsSentByBank(bankId);
        return ResponseEntity.ok(new MessageResponse("OTPs sent by bank: " + count, 200));
    }

    // Endpoint for PINs sent by bank
    @GetMapping("/pins/sent-by-bank/{bankId}")
    public ResponseEntity<?> getPINsSentByBank(@PathVariable Long bankId) {
        Long count = billingService.getPINsSentByBank(bankId);
        return ResponseEntity.ok(new MessageResponse("PINs sent by bank: " + count, 200));
    }
}
