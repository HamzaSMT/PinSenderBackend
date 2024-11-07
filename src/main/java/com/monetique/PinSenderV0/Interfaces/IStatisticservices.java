package com.monetique.PinSenderV0.Interfaces;

import com.monetique.PinSenderV0.payload.response.AgentStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.BankStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.OverallStatisticsResponse;

public interface IStatisticservices {
    void logSentItem(Long agentId, Long branchId, Long bankId, String type);

    // Bank-specific statistics
    BankStatisticsResponse getStatisticsForBank(Long bankId);

    // Agent-specific statistics
    AgentStatisticsResponse getStatisticsForAgent(Long agentId);

    // Overall statistics
    OverallStatisticsResponse getOverallStatistics();
}


