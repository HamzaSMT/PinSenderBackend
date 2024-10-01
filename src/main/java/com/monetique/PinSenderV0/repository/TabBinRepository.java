package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Banks.TabBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TabBinRepository extends JpaRepository<TabBin, String> {

    boolean existsTabBinByBin(String bin);
    Optional<TabBin> findByBin(String bin);
}
