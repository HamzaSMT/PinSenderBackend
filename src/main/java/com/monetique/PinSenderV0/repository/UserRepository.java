package com.monetique.PinSenderV0.repository;

import java.util.List;
import java.util.Optional;

import com.monetique.PinSenderV0.models.Users.ERole;
import com.monetique.PinSenderV0.models.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Boolean existsByUsername(String username);
  @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
  Optional<User> findByRole(ERole role);
  @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
  long countByRole(@Param("roleName") ERole roleName);

  List<User> findByAdminId(Long adminId);



 /* @Procedure(name = "FindUserByUsername")
  Optional<User> findByUsername(@Param("Username") String username);

  @Procedure(name = "ExistsByUsername")
  Boolean existsByUsername(@Param("Username") String username);

  @Procedure(name = "FindUserByRole")
  List<User> findByRole(@Param("Role") String role);

*/


}
