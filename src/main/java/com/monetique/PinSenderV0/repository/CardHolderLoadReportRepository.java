package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Card.CardHolderLoadReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardHolderLoadReportRepository extends JpaRepository<CardHolderLoadReport, Long> {
}
