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
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;
  // Keeps the refresh token's auth methods and adds the updated one
  private Boolean mfa;
}
