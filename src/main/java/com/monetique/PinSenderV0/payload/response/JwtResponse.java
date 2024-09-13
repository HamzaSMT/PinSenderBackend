package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
  private String token;
  private String type = "Bearer";
  private String refreshToken;
  private Long id;
  private String username;
  private List<String> roles;
  private Long adminId;
  private Long bankId;
  private Long agencyId;
  private Long sessionId; // Added for tracking

  // Constructor without sessionId
  public JwtResponse(String token, Long id, String username, List<String> roles) {
    this.token = token;
    this.id = id;
    this.username = username;
    this.roles = roles;
  }

  // Constructor with refreshToken, adminId, bankId, agencyId
  public JwtResponse(String token, String refreshToken, Long id, String username, List<String> roles, Long adminId, Long bankId, Long agencyId) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.roles = roles;
    this.adminId = adminId;
    this.bankId = bankId;
    this.agencyId = agencyId;
  }

  // Constructor with sessionId
  public JwtResponse(String token, String refreshToken, Long id, String username, List<String> roles, Long sessionId) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.roles = roles;
    this.sessionId = sessionId;  // Added sessionId
  }

  // Full constructor with refreshToken, sessionId, adminId, bankId, agencyId
  public JwtResponse(String token, String refreshToken, Long id, String username, List<String> roles, Long adminId, Long bankId, Long agencyId, Long sessionId) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.roles = roles;
    this.adminId = adminId;
    this.bankId = bankId;
    this.agencyId = agencyId;
    this.sessionId = sessionId;
  }
}
