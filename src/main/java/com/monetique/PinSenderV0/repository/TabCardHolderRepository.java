package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Card.TabCardHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TabCardHolderRepository extends JpaRepository<TabCardHolder, String> {

    // Method to find cardholders by agencyCode
    List<TabCardHolder> findByAgencyCode(String agencyCode);

    TabCardHolder findByCardNumber(String cardNumber);

    TabCardHolder findByClientNumber(String clientNumber);

    TabCardHolder findByCardHash(String cardHash);

    @Query("SELECT c.bankCode FROM TabCardHolder c WHERE c.cardHash = :cardHash")
    String findBankCodeByCardHash(@Param("cardHash") String cardHash);
}
