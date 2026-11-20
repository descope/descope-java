package com.descope.model.otp.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Where the OTP code was sent. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTPResponse {
  /** Masked email the code was sent to, set for the email delivery method. */
  private String maskedEmail;
  /** Masked phone the code was sent to, set for the phone delivery methods. */
  private String maskedPhone;
  /** Verification attempts, as returned by the server. */
  private Integer verificationAttempts;
}
