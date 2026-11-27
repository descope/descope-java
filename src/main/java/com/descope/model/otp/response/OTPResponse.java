package com.descope.model.otp.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OTPResponse {
  // Set when the code was sent by email
  private String maskedEmail;
  // Set when the code was sent to a phone
  private String maskedPhone;
  private Integer verificationAttempts;
}
