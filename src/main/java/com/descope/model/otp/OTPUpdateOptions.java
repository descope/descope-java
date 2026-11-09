package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPUpdateOptions {
  // Messaging provider to send the code with, up to 254 characters
  private String providerId;
  // Message locale, up to 254 characters
  private String locale;
  // Fail instead of merging when another user already has this email or phone
  private Boolean failOnConflict;
  // Keep the refresh token's auth methods and add the verified one to them
  private Boolean mfa;
}
