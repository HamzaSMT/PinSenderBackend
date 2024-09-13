package com.monetique.PinSenderV0.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private String message;
  private int statusCode;
}
