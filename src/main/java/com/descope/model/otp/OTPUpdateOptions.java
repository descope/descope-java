package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional OTP email or phone update parameters. Unset fields are not sent. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPUpdateOptions {
  /** ID of the messaging provider to send the code with. */
  private String providerId;
  /** ID of the message template to send the code with. */
  private String templateId;
  /** Locale of the message sent to the user. */
  private String locale;
  /** Fail the update on a conflict with another user. */
  private Boolean failOnConflict;
  /**
   * Keep the refresh token's existing auth methods and add the verified email or phone to them,
   * instead of replacing them. Requires a valid refresh token.
   */
  private Boolean mfa;
}
