package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankStatisticsResponse {
    private Long totalOtps;
    private Long totalPins;
    private List<Object[]> otpsByDate;
    private List<Object[]> pinsByDate;
    private List<Object[]> otpsByBranch;
    private List<Object[]> pinsByBranch;
    private List<Object[]> otpsByAgent;
    private List<Object[]> pinsByAgent;

    // Constructor, getters, and setters
}