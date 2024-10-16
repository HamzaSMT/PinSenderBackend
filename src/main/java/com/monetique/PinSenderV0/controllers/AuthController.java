package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Exception.TokenRefreshException;
import com.monetique.PinSenderV0.security.services.AuthenticationService;
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
import com.monetique.PinSenderV0.security.services.RefreshTokenService;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;

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
  AuthenticationManager authenticationManager;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  IuserManagementService iuserManagementService;

  @Autowired
  UserRepository userRepository;

  @Autowired
  BankRepository bankRepository;

  @Autowired
  RefreshTokenService refreshTokenService;

  @Autowired
  AgencyRepository agencyRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  private ItrackingingService monitoringService;




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
    } catch (Exception e) {
      // Handle other exceptions
      logger.error("Error during sign-in for username: {}", loginRequest.getUsername(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("Error: Internal server error", 500));
    }
  }

  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    logger.info("Received sign-out request.");

    // Check if the Authorization header is present
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body(new MessageResponse("Error: Missing or invalid Authorization header", 401));
    }

    // Extract JWT token from the Authorization header
    String jwtToken = authorizationHeader.substring(7); // Remove "Bearer " prefix

    try {
      // Delegate the sign-out logic to the SignOutService
      authenticationService.logoutUser(jwtToken);

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

////////////********************************usermanagement**********************************************/////////////////////////////////////////

  // Create Super Admin method
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
            encoder.encode(signUpRequest.getPassword()),  // Encode the password
            roles
    );

    // Save the new Super Admin to the repository
    userRepository.save(superAdmin);

    logger.info("Super Admin {} created successfully", signUpRequest.getUsername());
    return ResponseEntity.ok(new MessageResponse("Super Admin created successfully!", 200));
  }

  // Signup method (Register)
  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    logger.info("Received sign-up request for username: {}", signUpRequest.getUsername());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      logger.error("Username {} is already taken", signUpRequest.getUsername());
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!", 400));
    }


    UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
    User currentUser = userRepository.findById(currentUserDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    try {
      if (strRoles == null || strRoles.isEmpty()) {
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
        roles.add(userRole);
      } else {
        strRoles.forEach(role -> {
          switch (role) {
            case "admin":
              if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
                throw new AccessDeniedException("Error: Only Super Admins can create Admins.");
              }
              Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                      .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_ADMIN"));
              roles.add(adminRole);

              // Create Admin without mandatory bank association initially
              User adminUser = new User(signUpRequest.getUsername(), encoder.encode(signUpRequest.getPassword()),
                      roles, currentUser,null, null); // No bank, no agency
              userRepository.save(adminUser);
              logger.info("Admin {} created successfully", signUpRequest.getUsername());
              break;

            case "user":
              if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_ADMIN))) {
                throw new AccessDeniedException("Error: Only Admins can create Users.");
              }
              Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                      .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "ROLE_USER"));
              roles.add(userRole);

              // Create User without mandatory bank and agency association initially
              User user = new User(signUpRequest.getUsername(), encoder.encode(signUpRequest.getPassword()),
                      roles, currentUser, currentUser.getBank(), null); // Auto-associated to Admin's bank
              userRepository.save(user);
              logger.info("User {} created successfully", signUpRequest.getUsername());
              break;

            default:
              throw new AccessDeniedException("Error: Role not recognized.");
          }
        });
      }
    } catch (Exception e) {
      logger.error("Error during user registration: {}", signUpRequest.getUsername(), e);
      return ResponseEntity.status(500).body(new MessageResponse("Error: Unable to register user", 500));
    }

    return ResponseEntity.ok(new MessageResponse("User registered successfully!", 200));
  }

  @PostMapping("/associateAdminToBank")
  @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
  public ResponseEntity<?> associateAdminToBank(@RequestParam Long adminId, @RequestParam Long bankId) {
    logger.info("Received request to associate admin {} with bank {}", adminId, bankId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
    User currentUser = userRepository.findById(currentUserDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

    if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
      throw new AccessDeniedException("Error: Only Super Admin can associate Admins with Banks.");
    }

    User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));
    TabBank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", bankId));

    admin.setBank(bank);
    userRepository.save(admin);
   bank.setAdminUsername(admin.getUsername());
     bankRepository.save(bank);
    logger.info("Admin {} successfully associated with bank {}", adminId, bankId);
    return ResponseEntity.ok(new MessageResponse("Admin successfully associated with the bank!", 200));
  }


  @PostMapping("/associateUserToAgency")
  public ResponseEntity<?> associateUserToAgency(@RequestParam Long userId, @RequestParam Long agencyId) {
    logger.info("Received request to associate user {} with agency {}", userId, agencyId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
    User currentAdmin = userRepository.findById(currentUserDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", currentUserDetails.getId()));

    if (!currentAdmin.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_ADMIN))) {
      throw new AccessDeniedException("Error: Only Admins can associate Users with Agencies.");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    if (!user.getAdmin().getId().equals(currentAdmin.getId())) {
      throw new AccessDeniedException("Error: You can only associate your own users.");
    }

    Agency agency = agencyRepository.findById(agencyId)
            .orElseThrow(() -> new ResourceNotFoundException("Agency", "id", agencyId));

    if (!agency.getBank().getId().equals(currentAdmin.getBank().getId())) {
      throw new AccessDeniedException("Error: You can only associate users with agencies in your bank.");
    }

    user.setAgency(agency);
    userRepository.save(user);

    logger.info("User {} successfully associated with agency {}", userId, agencyId);
    return ResponseEntity.ok(new MessageResponse("User successfully associated with the agency!", 200));
  }

  @PostMapping("/changePassword")

  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    try {
      iuserManagementService.changePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
      return ResponseEntity.ok(new MessageResponse("Password changed successfully!", 200));
    } catch (Exception e) {
      return ResponseEntity.status(400).body(new MessageResponse(e.getMessage(), 400));
    }
  }
  @PostMapping("/forgetPassword")
  @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
  public ResponseEntity<?> generateRandomPassword(@RequestBody GeneratePasswordRequest request) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    try {
      String newPassword = iuserManagementService.generateRandomPassword(request.getUserId());
      return ResponseEntity.ok(new MessageResponse("Random password generated and saved successfully! New password: " + newPassword, 200));    } catch (Exception e) {
      return ResponseEntity.status(400).body(new MessageResponse(e.getMessage(), 400));
    }
  }

  @PutMapping("/update")
  public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest updateUserRequest) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    try {
      // Get authenticated user details

      UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
      Long userId = currentUserDetails.getId();
      // Update user details
      User updatedUser = iuserManagementService.updateUser(userId, updateUserRequest);
      logger.info("User {} updated successfully", updatedUser.getUsername());

      // Return success message
      return ResponseEntity.ok(new MessageResponse("User updated successfully!", 200));
    } catch (ResourceNotFoundException e) {
      logger.error("User not found: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage(), 404));
    } catch (Exception e) {
      logger.error("Error updating user details: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("Error updating user details", 500));
    }
  }
  @GetMapping("/users")
  // Ensure only admins can access this endpoint
  public ResponseEntity<?> getUsersByAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    try {
      List<UserResponseDTO> users = iuserManagementService.getUsersByAdmin();

      // Successful response with users list
      return ResponseEntity.ok(users);
    } catch (ResourceNotFoundException e) {
      // Handle case when no users are found
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new MessageResponse(e.getMessage(), 404));
    } catch (IllegalStateException e) {
      // Handle case when user is not authenticated
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated", 401));
    } catch (Exception e) {
      // Handle any other unexpected errors
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("Error retrieving users", 500));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable("id") Long userId) {
    logger.info("Received request to get user by ID: {}", userId);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new MessageResponse("User is not authenticated!", 401));
    }
    try {
      User user = iuserManagementService.getuserbyId(userId);
      logger.info("User found: {}", user);
      return ResponseEntity.ok(user); // Return the user object if found
    } catch (NoSuchElementException e) {
      logger.error("User not found with ID: {}", userId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(new MessageResponse("User not found", 404)); // 404 Not Found
    } catch (Exception e) {
      logger.error("Error retrieving user: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new MessageResponse("Error retrieving user", 500)); // 500 Internal Server Error
    }
  }


















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


