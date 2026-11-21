package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional OTP sign-up settings sent with the request. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPSignUpOptions {
  private String providerId;
  private String templateId;
}
