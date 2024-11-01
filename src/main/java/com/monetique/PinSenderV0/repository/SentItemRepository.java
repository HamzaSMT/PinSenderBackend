package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Statistique.SentitmePinOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SentItemRepository extends JpaRepository<SentitmePinOTP, Long> {

    // Method to find all uncounted OTPs
    List<SentitmePinOTP> findAllByCountedFalse();



    /*
    @Query("SELECT COUNT(s) FROM SentitmePinOTP s WHERE s.agentId = :agentId AND s.type = :type")
    Long countByAgentIdAndType(@Param("agentId") Long agentId, @Param("type") String type);

    @Query("SELECT COUNT(s) FROM SentitmePinOTP s WHERE s.branchId = :branchId AND s.type = :type")
    Long countByBranchIdAndType(@Param("branchId") Long branchId, @Param("type") String type);

    @Query("SELECT COUNT(s) FROM SentitmePinOTP s WHERE s.bankId = :bankId AND s.type = :type")
    Long countByBankIdAndType(@Param("bankId") Long bankId, @Param("type") String type);*/


}
