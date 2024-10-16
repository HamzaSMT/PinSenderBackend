package com.monetique.PinSenderV0.repository;



import com.monetique.PinSenderV0.models.Banks.Agency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {
   /* @Query("SELECT a FROM Agency a WHERE a.bank.admin.id = :adminId")
    List<Agency> findByAdminId(@Param("adminId") Long adminId);
    @Query("SELECT a FROM Agency a WHERE a.bank.id = (SELECT b.id FROM TabBank b WHERE b.admin.id = :adminId)")
    List<Agency> findByAdminId(@Param("adminId") Long adminId);*/
   @Query("SELECT a FROM Agency a LEFT JOIN FETCH a.users WHERE a.bank.id = :bankId")
   List<Agency> findByBankIdWithUsers(@Param("bankId") Long bankId);


}
