package com.monetique.PinSenderV0.repository;

import java.util.Optional;

import com.monetique.PinSenderV0.models.ERole;
import com.monetique.PinSenderV0.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
