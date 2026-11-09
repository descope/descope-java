package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPSignInOptions {
  // Messaging provider to send the code with
  private String providerId;
  private String ssoAppId;
  private Boolean useRecovery;
}
