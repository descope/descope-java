package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional OTP sign-in and sign-up-or-in parameters. Unset fields are not sent. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPSignInOptions {
  /** ID of the messaging provider to send the code with. */
  private String providerId;
  /** ID of the SSO application the user is signing in to. */
  private String ssoAppId;
  /** Use the user's recovery email or phone. */
  private Boolean useRecovery;
}
