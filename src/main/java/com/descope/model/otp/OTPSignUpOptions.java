package com.descope.model.otp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Optional OTP sign-up parameters. Unset fields are not sent. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPSignUpOptions {
  /** ID of the messaging provider to send the code with. */
  private String providerId;
  /** ID of the message template to send the code with. */
  private String templateId;
}
