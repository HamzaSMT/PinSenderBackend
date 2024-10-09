package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Banks.TabCardHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TabCardHolderRepository extends JpaRepository<TabCardHolder, String> {

    // Method to find cardholders by agencyCode
    List<TabCardHolder> findByAgencyCode(String agencyCode);

    // Method to find cardholders by bank_code
    List<TabCardHolder> findByBank_BankCode(String bankCode);
    boolean existsByCardNumberAndFinalDateAndNationalIdAndGsm(String cardNumber, String finalDate, String nationalId, String gsm);

    TabCardHolder findByCardNumber(String cardNumber);
}
