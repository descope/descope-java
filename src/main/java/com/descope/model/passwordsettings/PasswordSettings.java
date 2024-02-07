package com.descope.model.passwordsettings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordSettings {
  private Boolean enabled;
  private Integer minLength;
  private Boolean lowercase;
  private Boolean uppercase;
  private Boolean number;
  private Boolean nonAlphanumeric;
  private Boolean expiration;
  private Integer expirationWeeks;
  private Boolean reuse;
  private Integer reuseAmount;
  private Boolean lock;
  private Integer lockAttempts;
}
