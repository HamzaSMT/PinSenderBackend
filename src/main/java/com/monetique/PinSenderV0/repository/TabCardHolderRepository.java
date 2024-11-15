package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Card.TabCardHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TabCardHolderRepository extends JpaRepository<TabCardHolder, String> {

    // Method to find cardholders by agencyCode
    List<TabCardHolder> findByAgencyCode(String agencyCode);

    boolean existsByCardNumberAndFinalDateAndNationalIdAndGsm(String cardNumber, String finalDate, String nationalId, String gsm);

    TabCardHolder findByCardNumber(String cardNumber);

    TabCardHolder findByClientNumber(String clientNumber);
}
