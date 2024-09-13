package com.monetique.PinSenderV0.security.services;



import com.monetique.PinSenderV0.Exception.TokenRefreshException;
import com.monetique.PinSenderV0.models.RefreshToken;
import com.monetique.PinSenderV0.repository.RefreshTokenRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwtExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        // Check if a refresh token already exists for the user
        Optional<RefreshToken> existingToken = findByUserId(userId);

        if (existingToken.isPresent()) {
            // If a token exists, delete the old one
            refreshTokenRepository.delete(existingToken.get());

        }

        // Create a new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        // Save the new token and return it
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByUserId(Long userId) {
        return refreshTokenRepository.findByUserId(userId);
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new sign-in request.");
        }

        return token;
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
    }
}
