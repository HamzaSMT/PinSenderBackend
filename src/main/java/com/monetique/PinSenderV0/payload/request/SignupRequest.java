package com.monetique.PinSenderV0.payload.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 30)
  private String username;


  private String bankname;

  private Set<String> role;
  private String email;
  private String phoneNumber;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;



  private Long agencyId;
}
