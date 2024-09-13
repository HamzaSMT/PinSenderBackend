package com.monetique.PinSenderV0.repository;


import com.monetique.PinSenderV0.models.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepository extends JpaRepository<Bank, Long> {
}
