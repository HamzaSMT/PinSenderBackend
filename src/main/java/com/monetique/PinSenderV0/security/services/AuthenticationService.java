package com.monetique.PinSenderV0.security.services;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Exception.TokenRefreshException;
import com.monetique.PinSenderV0.models.Users.RefreshToken;
import com.monetique.PinSenderV0.models.Users.User;
import com.monetique.PinSenderV0.models.Users.UserSession;
import com.monetique.PinSenderV0.payload.request.LoginRequest;
import com.monetique.PinSenderV0.payload.response.JwtResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.TokenRefreshResponse;
import com.monetique.PinSenderV0.security.jwt.JwtUtils;

import com.monetique.PinSenderV0.tracking.ItrackingingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ItrackingingService monitoringService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;





    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        logger.info("Received sign-in request for username: {}", loginRequest.getUsername());

        // Check if the user already has an active session
        UserSession activeSession = monitoringService.getActiveSessionByUsername(loginRequest.getUsername());
        if (activeSession != null && activeSession.getLogoutTime() == null) {
            // Invalidate the old session
            monitoringService.endSession(activeSession.getId());
            logger.warn("Invalidated previous session for user {}.", loginRequest.getUsername());

            // Delete the old refresh token
            refreshTokenService.deleteByUserId(activeSession.getUser().getId());
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Start a new session for the user
        UserSession session = monitoringService.startSession(userDetails.getId());

        String jwt = jwtUtils.generateJwtToken(authentication, session.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), session.getId());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Create the response object
        return new JwtResponse(jwt,refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(), roles, session.getId());
    }

    public void logoutUser(String jwtToken) {
        // Validate and parse the token
        if (!jwtUtils.validateJwtToken(jwtToken)) {
            throw new RuntimeException("Invalid JWT token");
        }

        // Get the Authentication object from Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract user ID from the UserDetailsImpl
        Long userId = userDetails.getId();
        // Extract session ID from the JWT token claims
        Long sessionId = jwtUtils.getSessionIdFromJwtToken(jwtToken);

        // Fetch the session from the database
        UserSession session = monitoringService.getSessionById(sessionId);
        if (session == null) {
            throw new ResourceNotFoundException("Session not found");
        }

        // Check if the session is already ended
        if (session.getLogoutTime() != null) {
            throw new RuntimeException("Session already ended");
        }

        // Invalidate the session for the user
        monitoringService.endSession(sessionId);

        // Revoke the refresh token associated with the user
        refreshTokenService.deleteByUserId(userId);

        logger.info("User with ID {} signed out successfully.", userId);
    }

    public TokenRefreshResponse refreshToken(String requestRefreshToken) {
        // Attempt to find and verify the refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in the database or expired!"));

        // If valid, generate a new JWT token
        User user = refreshToken.getUser(); // Assuming refreshToken has getUser() method
        Long sessionId = refreshToken.getSessionId();

        // Generate a new JWT token using the user ID
        String newJwtToken = jwtUtils.generateTokenFromUsersession(user, sessionId);

        logger.info("New JWT token generated successfully for user with ID {}", user.getId());

        // Return the response object containing the new JWT and the existing refresh token
        return new TokenRefreshResponse(newJwtToken, refreshToken.getToken());
    }

}