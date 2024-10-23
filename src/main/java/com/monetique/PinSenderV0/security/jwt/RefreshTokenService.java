package com.monetique.PinSenderV0.security.jwt;



import com.monetique.PinSenderV0.Exception.TokenRefreshException;
import com.monetique.PinSenderV0.tracking.ItrackingingService;
import com.monetique.PinSenderV0.models.Users.RefreshToken;
import com.monetique.PinSenderV0.repository.RefreshTokenRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }
    @Autowired
    ItrackingingService monitoringService;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId,Long sessionid) {
        // Check if a refresh token already exists for the user
        Optional<RefreshToken> existingToken = findByUserId(userId);

        if (existingToken.isPresent()) {
            RefreshToken token = existingToken.get();

            // Check if the existing token is expired
            if (token.getExpiryDate().isAfter(Instant.now())) {
                // Token is not expired, return the existing token
                return token;
            }

            // If the token is expired, delete the old one
            refreshTokenRepository.delete(token);
        }

        // Create a new refresh token as none exist or it was expired
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setSessionId(sessionid);
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
    @Transactional
    public void deleteByUserId(Long userId) {

        refreshTokenRepository.deleteByUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
    }

    @Transactional
    public void deleteBysessionid(Long sessionid) {

        refreshTokenRepository.deleteBySessionId(sessionid);
    }


}
