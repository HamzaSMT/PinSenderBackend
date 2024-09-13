package com.monetique.PinSenderV0.repository;



import com.monetique.PinSenderV0.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    List<UserSession> findByLogoutTimeIsNull();  // Fetch active sessions
    List<UserSession> findByUser_Id(Long userId);
    @Query("SELECT s FROM UserSession s WHERE s.user.username = :username AND s.logoutTime IS NULL")
    UserSession findCurrentSessionByUsername(String username);
}
