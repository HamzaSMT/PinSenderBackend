package com.monetique.PinSenderV0.repository;



import com.monetique.PinSenderV0.models.Banks.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {
    @Query("SELECT a FROM Agency a WHERE a.bank.admin.id = :adminId")
    List<Agency> findByAdminId(@Param("adminId") Long adminId);
    List<Agency> findByBankId(Long bankId);

}
