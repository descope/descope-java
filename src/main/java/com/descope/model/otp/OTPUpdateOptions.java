package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional OTP email and phone update settings sent with the request. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPUpdateOptions {
  private String providerId;
  private String templateId;
  private String locale;
  private Boolean failOnConflict;
  /** Keep the refresh token's auth methods and add the verified one (requires a valid refresh token). */
  private Boolean mfa;
}
