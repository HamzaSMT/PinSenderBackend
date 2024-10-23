package com.monetique.PinSenderV0.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import com.monetique.PinSenderV0.Interfaces.IuserManagementService;
import com.monetique.PinSenderV0.models.Users.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtils {
  @Autowired
  IuserManagementService userManagementService;

  @Value("${app.jwtSecret}")
  private String jwtSecret;

  @Value("${app.jwtExpirationMs}")
  private int jwtExpirationMs;

  @Value("${app.jwtRefreshExpirationMs}")
  private int jwtRefreshExpirationMs;

  // Generate JWT token from Authentication object (used on login)
  public String generateJwtToken(Authentication authentication, Long sessionId) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

    return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("roles", userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
            .claim("adminId", userPrincipal.getAdmin() != null ? userPrincipal.getAdmin().getId() : null)
            .claim("bankId", userPrincipal.getBank() != null ? userPrincipal.getBank().getId() : null)
            .claim("agencyId", userPrincipal.getAgency() != null ? userPrincipal.getAgency().getId() : null)
            .claim("sessionId", sessionId)  // Add sessionId to JWT claims
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Access token expiration
            .signWith(key(), SignatureAlgorithm.HS512)
            .compact();
  }

  // Generate JWT token from username (used for refreshing token)
  public String generateTokenFromUsername(String username) {
    return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Access token expiration
            .signWith(key(), SignatureAlgorithm.HS512)
            .compact();
  }
    public String generateTokenFromUsersession(User user, Long sessionId) {

    return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("roles", user.getRoles().stream()
                    .map(role -> role.getName().name()) // Get role names
                    .collect(Collectors.toList()))
            .claim("adminId", user.getAdmin() != null ? user.getAdmin().getId() : null) // Set adminId if available
            .claim("bankId", user.getBank() != null ? user.getBank().getId() : null) // Set bankId if available
            .claim("agencyId", user.getAgency() != null ? user.getAgency().getId() : null) // Set agencyId if available
            .claim("sessionId", sessionId) // Set sessionId as a claim
            .setIssuedAt(new Date()) // Token issued time
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Token expiration time
            .signWith(key(), SignatureAlgorithm.HS512) // Sign the token with HS512 algorithm and key
            .compact();
  }


  // Extract the username from the JWT token
  public String getUserNameFromJwtToken(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }
  public Long getSessionIdFromJwtToken(String token) {
    Claims claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .getBody();
    return claims.get("sessionId", Long.class);
  }


  // Validate the JWT token
  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
      return true;
    } catch (MalformedJwtException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("JWT claims string is empty: {}", e.getMessage());
    }
    return false;
  }

  // Private method to decode and return the secret key
  private Key key() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
  }
}
