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
public class AgentStatisticsResponse {
    private Long totalOtps;
    private Long totalPins;
    private List<Object[]> otpsByDate;
    private List<Object[]> pinsByDate;

    // Constructor, getters, and setters
}

