package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Exception.TokenRefreshException;
import com.monetique.PinSenderV0.security.jwt.AuthenticationService;
import com.monetique.PinSenderV0.tracking.ItrackingingService;
import com.monetique.PinSenderV0.Interfaces.IuserManagementService;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import com.monetique.PinSenderV0.models.Users.*;
import com.monetique.PinSenderV0.payload.request.*;
import com.monetique.PinSenderV0.payload.response.JwtResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.TokenRefreshResponse;
import com.monetique.PinSenderV0.payload.response.UserResponseDTO;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.RoleRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.security.jwt.JwtUtils;
import com.monetique.PinSenderV0.security.jwt.RefreshTokenService;
import com.monetique.PinSenderV0.security.jwt.UserDetailsImpl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import jakarta.validation.Valid;
import org.springframework.web.util.WebUtils;


import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


  @Autowired
  private AuthenticationService authenticationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    IuserManagementService userManagementService;

  @PostMapping("/createSuperAdmin")
  public ResponseEntity<?> createSuperAdmin(@Valid @RequestBody SignupRequest signUpRequest) {
    logger.info("Received Super Admin creation request for username: {}", signUpRequest.getUsername());

    // Check if the username already exists
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      logger.error("Username {} is already taken", signUpRequest.getUsername());
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!", 400));
    }
    if (userRepository.countByRole(ERole.ROLE_SUPER_ADMIN) > 0) {
      logger.error("A Super Admin already exists, cannot create another.");
      return ResponseEntity.badRequest().body(new MessageResponse("Error: A Super Admin already exists!", 400));
    }
    // Add the Super Admin role to the new user
    Set<Role> roles = new HashSet<>();
    Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPER_ADMIN)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_SUPER_ADMIN"));
    roles.add(superAdminRole);

    // Create the new Super Admin user without admin, bank, or agency
    User superAdmin = new User(
            signUpRequest.getUsername(),
            passwordEncoder.encode(signUpRequest.getPassword()),  // Encode the password
            roles
    );

    // Save the new Super Admin to the repository
    userRepository.save(superAdmin);

    logger.info("Super Admin {} created successfully", signUpRequest.getUsername());
    return ResponseEntity.ok(new MessageResponse("Super Admin created successfully!", 200));
  }




  // Signout method (Logout)
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    try {
      // Attempt to authenticate the user
      JwtResponse jwtResponse = authenticationService.authenticateUser(loginRequest);
      logger.info("User {} signed in successfully.", loginRequest.getUsername());
      ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", jwtResponse.getRefreshToken())
              .httpOnly(true)
              .secure(true) // Enable for HTTPS
              .path("/api/auth/refreshToken")
              .maxAge(7 * 24 * 60 * 60) // Example: 7 days
              .sameSite("Strict") // CSRF protection
              .build();

      // Return the response with the refresh token cookie
      return ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()) // Set the cookie in the response
              .body(jwtResponse);
    } catch (BadCredentialsException e) {
      // Handle invalid username or password
      logger.error("Invalid username or password for username: {}", loginRequest.getUsername());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new MessageResponse("Error: Invalid username or password", 400));
    } catch (RuntimeException e) {
      if (e.getMessage().equals("User account is inactive.")) {
        logger.warn("Inactive account for username: {}", loginRequest.getUsername());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse("Error: User account is inactive", 403));
      }
      throw e;
    } catch (Exception e) {
      // Handle other exceptions
      logger.error("Error during sign-in for username: {}", loginRequest.getUsername(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("Error: Internal server error", 500));
    }
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser(@RequestParam Long SessionId) {
    logger.info("Received sign-out request.");


    try {
      // Delegate the sign-out logic to the SignOutService
      authenticationService.logoutUser(SessionId);

      // Create a cookie to delete the refresh token
      ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
              .httpOnly(true)
              .secure(true) // Enable for HTTPS
              .path("/api/auth/refreshToken")
              .maxAge(0) // Set the cookie to expire immediately
              .sameSite("Strict") // CSRF protection
              .build();

      logger.info("User signed out successfully.");
      return ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()) // Set the cookie in the response
              .body(new MessageResponse("You've been signed out successfully!", 200));
    } catch (RuntimeException e) {
      logger.error("Error during sign-out: {}", e.getMessage());
      return ResponseEntity.status(400).body(new MessageResponse("Error: " + e.getMessage(), 400));
    } catch (Exception e) {
      logger.error("Error during sign-out: {}", e.getMessage(), e);
      return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to sign out due to a server error", 500));
    }
  }
  @PostMapping("/refreshToken")
  public ResponseEntity<?> refreshToken(HttpServletRequest request) {
    logger.info("Received request to refresh token");

    // Retrieve refresh token from the request cookie
    Cookie refreshTokenCookie = WebUtils.getCookie(request, "refreshToken");

    if (refreshTokenCookie == null) {
      throw new TokenRefreshException(null, "Missing refresh token in request");
    }

    String requestRefreshToken = refreshTokenCookie.getValue();

    try {
      // Delegate the token refresh logic to the TokenRefreshService
      TokenRefreshResponse response = authenticationService.refreshToken(requestRefreshToken);

      // Return the response with the new refresh token cookie
      return ResponseEntity.ok()
              .body(response); // Return the new JWT and refresh token details
    } catch (TokenRefreshException e) {
      logger.error("Error refreshing token: {}", e.getMessage());
      return ResponseEntity.status(400).body(new MessageResponse("Error: " + e.getMessage(), 400));
    } catch (Exception e) {
      logger.error("Error during token refresh: {}", e.getMessage());
      return ResponseEntity.status(500).body(new MessageResponse("Error: Internal server error", 500));
    }
  }
  @PostMapping("superadmin/forgetPassword")

  public ResponseEntity<?> generateRandomPassword(@RequestBody GeneratePasswordRequest request) {
    // Validate the request object and user ID
    if (request == null || request.getUserId() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new MessageResponse("User ID is required.", 400));
    }

    try {
      // Generate a random password for the specified user
      String newPassword = userManagementService.generateRandomPassword(request.getUserId());

      // Check if password generation was successful
      if (newPassword == null || newPassword.isEmpty()) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Failed to generate a new password.", 500));
      }

      // Successfully generated and saved the password
      return ResponseEntity.ok(newPassword);

    } catch (ResourceNotFoundException e) {
      // Handle the case where the user is not found
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new MessageResponse("User not found with ID: " + request.getUserId(), 404));

    } catch (Exception e) {
      // Handle any other unexpected exceptions
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("An error occurred: " + e.getMessage(), 500));
    }
  }

////////////********************************usermanagement**********************************************/////////////////////////////////////////



















//////////////////***************************OLDimpl**********************************************////////////////////////////////

/*
@PostMapping("/signout")
  public ResponseEntity<?> logoutUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    logger.info("Received sign-out request.");

    // Check if the Authorization header is present
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body(new MessageResponse("Error: Missing or invalid Authorization header", 401));
    }

    try {
      // Extract JWT token from the Authorization header
      String jwtToken = authorizationHeader.substring(7); // Remove "Bearer " prefix

      // Validate and parse the token
      if (!jwtUtils.validateJwtToken(jwtToken)) {
        return ResponseEntity.status(401).body(new MessageResponse("Error: Invalid JWT token", 401));
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
        return ResponseEntity.status(404).body(new MessageResponse("Error: Session not found", 404));
      }

      // Check if the session is already ended
      if (session.getLogoutTime() != null) {
        return ResponseEntity.status(400).body(new MessageResponse("Error: Session already ended", 400));
      }

      // Invalidate the session for the user
      monitoringService.endSession(sessionId);

      // Revoke the refresh token associated with the user
      refreshTokenService.deleteByUserId(userId);

      logger.info("User with ID {} signed out successfully.", userId);

      return ResponseEntity.ok(new MessageResponse("You've been signed out successfully!", 200));
    } catch (Exception e) {
      logger.error("Error during sign-out: {}", e.getMessage(), e);
      return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to sign out due to a server error", 500));
    }






  }




@PostMapping("/refreshToken")
  public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
    logger.info("Received request to refresh token");

    String requestRefreshToken = request.getRefreshToken();

    return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
              String token = jwtUtils.generateTokenFromUsername(user.getUsername());
              logger.info("Token refreshed successfully for user {}", user.getUsername());
              return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in the database!"));
  }
   // Signin method (Login)
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    logger.info("Received sign-in request for username: {}", loginRequest.getUsername());

    try {
      // Check if the user already has an active session
      UserSession activeSession = monitoringService.getActiveSessionByUsername(loginRequest.getUsername());
      if (activeSession != null && activeSession.getLogoutTime() == null) {
        logger.warn("User {} already has an active session.", loginRequest.getUsername());
        return ResponseEntity.status(403).body(new MessageResponse("Error: Another session is already opened for this user.", 403));

      }
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

      // Start a new session for the user
      UserSession session = monitoringService.startSession(userDetails.getId());

      String jwt = jwtUtils.generateJwtToken(authentication, session.getId());  // Pass sessionId

      List<String> roles = userDetails.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());
      System.out.println("iduser"+userDetails.getId() );
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), session.getId());
      ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
              .httpOnly(true)
              .secure(true) // Enable for HTTPS
              .path("/api/auth/refreshToken")
              .maxAge(7 * 24 * 60 * 60) // Example: 7 days
              .sameSite("Strict") // CSRF protection
              .build();

      logger.info("User {} signed in successfully.", loginRequest.getUsername());
      return ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()) // Set the cookie in the response
              .body(new JwtResponse(
                      jwt,
                      refreshToken.getToken(),
                      userDetails.getId(),
                      userDetails.getUsername(),
                      roles,
                      session.getId()  // Return session ID to track API usage
              ));
    } catch (BadCredentialsException e) {
      // Handle incorrect username or password
      logger.error("Invalid username or password for username: {}", loginRequest.getUsername());
      return ResponseEntity.status(401).body(new MessageResponse("Error: Invalid username or password", 401));
    } catch (Exception e) {
      logger.error("Error during sign-in for username: {}", loginRequest.getUsername(), e);
      return ResponseEntity.status(500).body(new MessageResponse("Error: Internal server error", 500));
    }
  }
 @PostMapping("/signin2")
  public ResponseEntity<?> authenticateUser2(@Valid @RequestBody LoginRequest loginRequest) {

    logger.info("Received sign-in request for username: {}", loginRequest.getUsername());

    try {
      // Fetch the user by username
      Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());

      // Check if user exists
      if (!userOptional.isPresent()) {
        logger.error("User not found for username: {}", loginRequest.getUsername());
        return ResponseEntity.status(404).body(new MessageResponse("Error: User not found", 404));
      }

      User user = userOptional.get();

      // Check if the user already has an active session
      UserSession activeSession = monitoringService.getActiveSessionByUsername(loginRequest.getUsername());

      // Retrieve the refresh token using the user's ID
      Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByUserId(user.getId());

      // Check for active session and refresh token
      if (activeSession != null) {
        logger.warn("User {} already has an active session.", loginRequest.getUsername());
        if (refreshTokenOptional.isPresent()) {
          // Verify expiration of the existing refresh token
          RefreshToken refreshToken = refreshTokenOptional.get();
          try {
            refreshTokenService.verifyExpiration(refreshToken);
            monitoringService.endSession(activeSession.getId());
          } catch (TokenRefreshException e) {
            // If the token is expired, end the session
            monitoringService.endSession(activeSession.getId());
            logger.warn("Refresh token for user {} is expired. Ending active session.", loginRequest.getUsername());
            // Proceed to allow user to reconnect
          }
        } else {
          // No refresh token found, end the active session
          monitoringService.endSession(activeSession.getId());
          logger.warn("No refresh token found for user {}. Ending active session.", loginRequest.getUsername());
          // Proceed to allow user to reconnect
        }
      }

      // Authenticate the user
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

      // Start a new session for the user
      UserSession session = monitoringService.startSession(userDetails.getId());

      String jwt = jwtUtils.generateJwtToken(authentication, session.getId());  // Pass sessionId

      List<String> roles = userDetails.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());

      // Create a new refresh token
      RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(userDetails.getId(),session.getId());

      logger.info("User {} signed in successfully.", loginRequest.getUsername());

      return ResponseEntity.ok(new JwtRe  sponse(
              jwt,
              newRefreshToken.getToken(),
              userDetails.getId(),
              userDetails.getUsername(),
              roles,
              session.getId()  // Return session ID to track API usage
      ));
    } catch (BadCredentialsException e) {
      // Handle incorrect username or password
      logger.error("Invalid username or password for username: {}", loginRequest.getUsername());
      return ResponseEntity.status(401).body(new MessageResponse("Error: Invalid username or password", 401));
    } catch (TokenRefreshException e) {
      logger.error("Error during token refresh for user: {}", loginRequest.getUsername(), e);
      return ResponseEntity.status(403).body(new MessageResponse("Error: " + e.getMessage(), 403));
    } catch (Exception e) {
      logger.error("Error during sign-in for username: {}", loginRequest.getUsername(), e);
      return ResponseEntity.status(500).body(new MessageResponse("Error: Internal server error", 500));
    }
  }

  */



}


