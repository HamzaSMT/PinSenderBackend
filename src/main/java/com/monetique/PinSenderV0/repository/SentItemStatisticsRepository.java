package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Statistique.SentItemStatistics;
import com.monetique.PinSenderV0.payload.response.BankStatisticsResponse;
import com.monetique.PinSenderV0.payload.response.DateCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SentItemStatisticsRepository extends JpaRepository<SentItemStatistics, Long> {

    Optional<SentItemStatistics> findByBankIdAndBranchIdAndAgentIdAndTypeAndDate(Long bankId, Long branchId, Long agentId, String type, LocalDate date);

    // Total OTPs sent for a specific bank
    @Query("SELECT SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP' AND s.bankId = :bankId")
    Long countOtpsByBank(@Param("bankId") Long bankId);

    // Total PINs sent for a specific bank
    @Query("SELECT SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN' AND s.bankId = :bankId")
    Long countPinsByBank(@Param("bankId") Long bankId);

    // Total OTPs grouped by date for a specific bank
    @Query("SELECT s.date, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP' AND s.bankId = :bankId GROUP BY s.date")
    List<Object[]> countOtpsByDateForBank(@Param("bankId") Long bankId);

    // Total PINs grouped by date for a specific bank
    @Query("SELECT s.date, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN' AND s.bankId = :bankId GROUP BY s.date")
    List<Object[]> countPinsByDateForBank(@Param("bankId") Long bankId);

    // Total OTPs grouped by branch for a specific bank
    @Query("SELECT s.branchName, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP' AND s.bankId = :bankId GROUP BY s.branchName")
    List<Object[]> countOtpsByBranchForBank(@Param("bankId") Long bankId);

    // Total PINs grouped by branch for a specific bank
    @Query("SELECT s.branchName, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN' AND s.bankId = :bankId GROUP BY s.branchName")
    List<Object[]> countPinsByBranchForBank(@Param("bankId") Long bankId);

    // Total OTPs grouped by agent for a specific bank
    @Query("SELECT s.agentName, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP' AND s.bankId = :bankId GROUP BY  s.agentName")
    List<Object[]> countOtpsByAgentForBank(@Param("bankId") Long bankId);

    // Total PINs grouped by agent for a specific bank
    @Query("SELECT s.agentName, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN' AND s.bankId = :bankId GROUP BY s.agentName")
    List<Object[]> countPinsByAgentForBank(@Param("bankId") Long bankId);

    // Agent-specific total OTPs
    @Query("SELECT SUM(s.totalSent) FROM SentItemStatistics s WHERE s.agentId = :agentId AND s.type = 'OTP'")
    Long countOtpsByAgent(@Param("agentId") Long agentId);

    // Agent-specific total PINs
    @Query("SELECT SUM(s.totalSent) FROM SentItemStatistics s WHERE s.agentId = :agentId AND s.type = 'PIN'")
    Long countPinsByAgent(@Param("agentId") Long agentId);

    // Total OTPs grouped by date for a specific agent
    @Query("SELECT s.date, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.agentId = :agentId AND s.type = 'OTP' GROUP BY s.date")
    List<Object[]> countOtpsByDateForAgent(@Param("agentId") Long agentId);

    // Total PINs grouped by date for a specific agent
    @Query("SELECT s.date, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.agentId = :agentId AND s.type = 'PIN' GROUP BY s.date")
    List<Object[]> countPinsByDateForAgent(@Param("agentId") Long agentId);

    // Overall count of all OTPs sent across all banks
    @Query("SELECT SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP'")
    Long countAllOtps();

    // Overall count of all PINs sent across all banks
    @Query("SELECT SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN'")
    Long countAllPins();

    // Counts grouped by each bank for OTPs
    @Query("SELECT s.bankName,s.bankId, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP' GROUP BY s.bankId, s.bankName")
    List<Object[]> countOtpsGroupedByBank();

    // Counts grouped by each bank for PINs
    @Query("SELECT s.bankName,s.bankId, SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN' GROUP BY s.bankId, s.bankName")
    List<Object[]> countPinsGroupedByBank();

    // Counts grouped by each bank for OTPs and by date
    @Query("SELECT s.date, s.bankName,s.bankId , SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'OTP' GROUP BY s.date, s.bankId, s.bankName")
    List<Object[]> countOtpsGroupedByBankAndDate();

    // Counts grouped by each bank for PINs and by date
    @Query("SELECT s.date, s.bankName,s.bankId ,SUM(s.totalSent) FROM SentItemStatistics s WHERE s.type = 'PIN' GROUP BY s.date, s.bankId, s.bankName")
    List<Object[]> countPinsGroupedByBankAndDate();
}
