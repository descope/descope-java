package com.descope.model.password;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetOptions {
  /** Messaging provider to send with, at most 254 characters. */
  private String providerId;
  /** Message locale, at most 254 characters. */
  private String locale;
  private Boolean useRecovery;
}
