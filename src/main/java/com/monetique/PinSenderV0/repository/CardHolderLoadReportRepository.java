package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Banks.CardHolderLoadReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardHolderLoadReportRepository extends JpaRepository<CardHolderLoadReport, Long> {
}
