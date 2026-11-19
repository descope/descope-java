package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request options for OTP sign-up, sent next to the sign-up login options. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPSignUpOptions {
  private String providerId;
  private String templateId;
}
