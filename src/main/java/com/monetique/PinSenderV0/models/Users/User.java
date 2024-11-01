package com.monetique.PinSenderV0.models.Users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.monetique.PinSenderV0.models.Banks.Agency;
import com.monetique.PinSenderV0.models.Banks.TabBank;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String username;

  @NotBlank
  private String password;
  private String email;
  private String phoneNumber;
  private boolean active=true;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "user_roles",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @JsonIgnore // Prevent serialization to avoid infinite recursion
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "admin_id", nullable = true) // Nullable: Super Admin and Admin might not have an Admin
  private User admin;

  // Admin has one bank
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "bank_id", nullable = true)
  private TabBank bank;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "agency_id", nullable = true) // Nullable for users not yet associated with an agency
  private Agency agency;

  // Constructor for general users
  public User(String username, String password,Set<Role> roles, User admin, TabBank bank, Agency agency) {
    this.username = username;
    this.password = password;
    this.roles = roles;
    this.admin = admin;
    this.bank = bank;
    this.agency = agency;
  }

  // Constructor for Super Admin without bank or agency
  public User(String username, String password, Set<Role> roles) {
    this.username = username;
    this.password = password;
    this.roles = roles;
  }

  public User() {
  }

}
