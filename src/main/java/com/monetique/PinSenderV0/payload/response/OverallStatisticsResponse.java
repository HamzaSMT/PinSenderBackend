package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OverallStatisticsResponse {
    private Long overallOtps;
    private Long overallPins;
    private List<Object[]> otpsGroupedByBank;
    private List<Object[]> pinsGroupedByBank;
    private List<Object[]> otpsGroupedByBankAndDate;
    private List<Object[]> pinsGroupedByBankAndDate;

    // Constructor, getters, and setters
}
