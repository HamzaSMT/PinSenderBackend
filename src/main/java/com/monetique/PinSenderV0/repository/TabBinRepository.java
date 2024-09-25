package com.monetique.PinSenderV0.repository;

import com.monetique.PinSenderV0.models.Banks.TabBin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TabBinRepository extends JpaRepository<TabBin, String> {

    boolean existsTabBinByBin(String bin);
}
