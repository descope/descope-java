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
  private String providerId;
  private String templateId;
  private String ssoAppId;
  private Boolean useRecovery;
}
