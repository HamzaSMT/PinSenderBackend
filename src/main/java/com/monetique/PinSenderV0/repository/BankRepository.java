package com.monetique.PinSenderV0.repository;


import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankRepository extends JpaRepository<TabBank, Long> {

    Optional<TabBank>  findBybankCode(String bankCode);


    boolean existsTabBankBybankCode(String bankCode);

}
