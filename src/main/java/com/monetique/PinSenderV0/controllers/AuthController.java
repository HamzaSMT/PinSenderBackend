package com.monetique.PinSenderV0.controllers;

import com.monetique.PinSenderV0.Exception.AccessDeniedException;
import com.monetique.PinSenderV0.Exception.ResourceNotFoundException;
import com.monetique.PinSenderV0.Exception.TokenRefreshException;
import com.monetique.PinSenderV0.models.*;
import com.monetique.PinSenderV0.payload.request.LoginRequest;
import com.monetique.PinSenderV0.payload.request.SignupRequest;
import com.monetique.PinSenderV0.payload.request.TokenRefreshRequest;
import com.monetique.PinSenderV0.payload.response.JwtResponse;
import com.monetique.PinSenderV0.payload.response.MessageResponse;
import com.monetique.PinSenderV0.payload.response.TokenRefreshResponse;
import com.monetique.PinSenderV0.repository.AgencyRepository;
import com.monetique.PinSenderV0.repository.BankRepository;
import com.monetique.PinSenderV0.repository.RoleRepository;
import com.monetique.PinSenderV0.repository.UserRepository;
import com.monetique.PinSenderV0.security.jwt.JwtUtils;
import com.monetique.PinSenderV0.security.services.MonitoringService;
import com.monetique.PinSenderV0.security.services.RefreshTokenService;
import com.monetique.PinSenderV0.security.services.UserDetailsImpl;
import org.springframework.security.core.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  AuthenticationManager authenticationManager;

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
  private MonitoringService monitoringService;


  // Signin method (Login)
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    logger.info("Received sign-in request for username: {}", loginRequest.getUsername());

    try {
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtUtils.generateJwtToken(authentication);

      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      List<String> roles = userDetails.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .collect(Collectors.toList());
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

      // Start a new session for the user
      UserSession session = monitoringService.startSession(userDetails.getId());

      logger.info("User {} signed in successfully.", loginRequest.getUsername());

      return ResponseEntity.ok(new JwtResponse(
              jwt,
              refreshToken.getToken(),
              userDetails.getId(),
              userDetails.getUsername(),
              roles,
              session.getId()  // Return session ID to track API usage
      ));
    } catch (Exception e) {
      logger.error("Error during sign-in for username: {}", loginRequest.getUsername(), e);
      return ResponseEntity.status(401).body(new MessageResponse("Error: Unauthorized", 401));
    }
  }


  // Create Super Admin method
  @PostMapping("/createSuperAdmin")
  public ResponseEntity<?> createSuperAdmin(@Valid @RequestBody SignupRequest signUpRequest) {
    logger.info("Received Super Admin creation request for username: {}", signUpRequest.getUsername());

    // Check if the username already exists
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      logger.error("Username {} is already taken", signUpRequest.getUsername());
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!", 400));
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

    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      logger.error("Username {} is already taken", signUpRequest.getUsername());
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!", 400));
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
                      roles, currentUser, null, null); // No bank, no agency
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
  public ResponseEntity<?> associateAdminToBank(@RequestParam Long adminId, @RequestParam Long bankId) {
    logger.info("Received request to associate admin {} with bank {}", adminId, bankId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserDetailsImpl currentUserDetails = (UserDetailsImpl) authentication.getPrincipal();
    User currentUser = userRepository.findById(currentUserDetails.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserDetails.getId()));

    if (!currentUser.getRoles().stream().anyMatch(r -> r.getName().equals(ERole.ROLE_SUPER_ADMIN))) {
      throw new AccessDeniedException("Error: Only Super Admin can associate Admins with Banks.");
    }

    User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminId));
    Bank bank = bankRepository.findById(bankId)
            .orElseThrow(() -> new ResourceNotFoundException("Bank", "id", bankId));

    bank.setAdmin(admin); // Associate the admin with the bank
    bankRepository.save(bank);

    admin.setBank(bank);
    userRepository.save(admin);

    logger.info("Admin {} successfully associated with bank {}", adminId, bankId);
    return ResponseEntity.ok(new MessageResponse("Admin successfully associated with the bank!", 200));
  }


  @PostMapping("/associateUserToAgency")
  public ResponseEntity<?> associateUserToAgency(@RequestParam Long userId, @RequestParam Long agencyId) {
    logger.info("Received request to associate user {} with agency {}", userId, agencyId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
}
