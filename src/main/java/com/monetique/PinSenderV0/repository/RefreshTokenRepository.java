package com.monetique.PinSenderV0.repository;



import com.monetique.PinSenderV0.models.Users.RefreshToken;
import com.monetique.PinSenderV0.models.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
    Optional<RefreshToken> findByUserId(Long userId);
}