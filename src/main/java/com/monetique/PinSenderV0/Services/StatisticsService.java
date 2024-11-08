package com.monetique.PinSenderV0.Services;

import com.monetique.PinSenderV0.Interfaces.*;
import com.monetique.PinSenderV0.models.Statistique.SentItemKey;
import com.monetique.PinSenderV0.models.Statistique.SentItemStatistics;
import com.monetique.PinSenderV0.models.Statistique.SentitmePinOTP;
import com.monetique.PinSenderV0.payload.response.AgentStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.BankStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.OverallStatisticsResponse;
import com.monetique.PinSenderV0.repository.SentItemRepository;
import com.monetique.PinSenderV0.repository.SentItemStatisticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService implements IStatisticservices {
    @Autowired
    private SentItemStatisticsRepository statisticsRepository;
    @Autowired
    private SentItemRepository sentItemRepository;
    @Autowired
    private IbankService bankService;
    @Autowired
    private Iagencyservices agencyServices;
    @Autowired
    private IuserManagementService userManagementService;

    @Scheduled(cron = "0 0 20 * * ?")

    public void updateStatistics() {
        List<SentitmePinOTP> sentItems = sentItemRepository.findAllByCountedFalse();

        // Aggregate statistics
        Map<SentItemKey, Long> summaryMap = new HashMap<>();
        for (SentitmePinOTP item : sentItems) {
            SentItemKey key = new SentItemKey(item.getBankId(), item.getBranchId(), item.getAgentId(), item.getType());
            summaryMap.put(key, summaryMap.getOrDefault(key, 0L) + 1);
        }

        // Update the summary table
        for (Map.Entry<SentItemKey, Long> entry : summaryMap.entrySet()) {
            // Retrieve the names based on IDs
            String bankName = entry.getKey().getBankId() != null
                    ? bankService.getBankByIdforall(entry.getKey().getBankId()).getName()
                    : "Unknown";

            String branchName = entry.getKey().getBranchId() != null
                    ? agencyServices.getAgencyByIdforall(entry.getKey().getBranchId()).getName()
                    : "Unknown";
            String agentName = userManagementService.getuserbyId(entry.getKey().getAgentId()).getUsername();

            // Find or create the SentItemStatistics object
            SentItemStatistics stats = statisticsRepository
                    .findByBankIdAndBranchIdAndAgentIdAndTypeAndDate(
                            entry.getKey().getBankId(),
                            entry.getKey().getBranchId(),
                            entry.getKey().getAgentId(),
                            entry.getKey().getType(),
                            LocalDate.now()
                    )
                    .orElse(new SentItemStatistics(
                            entry.getKey().getBankId(),
                            entry.getKey().getBranchId(),
                            entry.getKey().getAgentId(),
                            bankName,
                            branchName,
                            agentName,
                            entry.getKey().getType(),
                            LocalDate.now(),
                            0L // Initial total sent count
                    ));

            // Update total sent count and names
            stats.setTotalSent(stats.getTotalSent() + entry.getValue());
            stats.setBankName(bankName); // Set the bank name
            stats.setBranchName(branchName); // Set the branch name
            stats.setAgentName(agentName); // Set the agent name

            statisticsRepository.save(stats);
        }
        for (SentitmePinOTP item : sentItems) {
            item.setCounted(true); // Marquer comme comptabilisé
        }
        sentItemRepository.saveAll(sentItems);
    }



    // Log the sent item (OTP or PIN)
    @Override
    public void logSentItem(Long agentId, Long branchId, Long bankId, String type) {
        SentitmePinOTP sentItem = new SentitmePinOTP();
        sentItem.setAgentId(agentId);
        sentItem.setBranchId(branchId);
        sentItem.setBankId(bankId);
        sentItem.setType(type);
        sentItemRepository.save(sentItem);
    }


    // Bank-specific statistics
    @Override
    public BankStatisticsResponse getStatisticsForBank(Long bankId) {
        Long totalOtps = statisticsRepository.countOtpsByBank(bankId);
        Long totalPins = statisticsRepository.countPinsByBank(bankId);
        List<Object[]> otpsByDate = statisticsRepository.countOtpsByDateForBank(bankId);
        List<Object[]> pinsByDate = statisticsRepository.countPinsByDateForBank(bankId);
        List<Object[]> otpsByBranch = statisticsRepository.countOtpsByBranchForBank(bankId);
        List<Object[]> pinsByBranch = statisticsRepository.countPinsByBranchForBank(bankId);
        List<Object[]> otpsByAgent = statisticsRepository.countOtpsByAgentForBank(bankId);
        List<Object[]> pinsByAgent = statisticsRepository.countPinsByAgentForBank(bankId);

        return new BankStatisticsResponse(totalOtps, totalPins, otpsByDate, pinsByDate, otpsByBranch, pinsByBranch, otpsByAgent, pinsByAgent);
    }

    // Agent-specific statistics
    @Override
    public AgentStatisticsResponse getStatisticsForAgent(Long agentId) {
        Long totalOtps = statisticsRepository.countOtpsByAgent(agentId);
        Long totalPins = statisticsRepository.countPinsByAgent(agentId);
        List<Object[]> otpsByDate = statisticsRepository.countOtpsByDateForAgent(agentId);
        List<Object[]> pinsByDate = statisticsRepository.countPinsByDateForAgent(agentId);

        return new AgentStatisticsResponse(totalOtps, totalPins, otpsByDate, pinsByDate);
    }

    // Overall statistics
    @Override
    public OverallStatisticsResponse getOverallStatistics() {
        Long overallOtps = statisticsRepository.countAllOtps();
        Long overallPins = statisticsRepository.countAllPins();
        List<Object[]> otpsGroupedByBank = statisticsRepository.countOtpsGroupedByBank();
        List<Object[]> pinsGroupedByBank = statisticsRepository.countPinsGroupedByBank();
        List<Object[]> otpsGroupedByBankAndDate = statisticsRepository.countOtpsGroupedByBankAndDate();
        List<Object[]> pinsGroupedByBankAndDate = statisticsRepository.countPinsGroupedByBankAndDate();

        return new OverallStatisticsResponse(overallOtps, overallPins, otpsGroupedByBank, pinsGroupedByBank, otpsGroupedByBankAndDate, pinsGroupedByBankAndDate);
    }


}
