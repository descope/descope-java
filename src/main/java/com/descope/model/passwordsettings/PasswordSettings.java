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
  /** Returned by get settings; configure settings takes the tenant ID as a separate argument. */
  private String tenantId;
  private Boolean tempLock;
  private Integer tempLockAttempts;
  private Integer tempLockDuration;
  private Boolean enablePasswordStrength;
  private Integer passwordStrengthScore;
  private Boolean anyLetter;
  private String disallowedCharacters;
  private Boolean disallowEmailMatch;

  public PasswordSettings(Boolean enabled, Integer minLength, Boolean lowercase, Boolean uppercase, Boolean number,
      Boolean nonAlphanumeric, Boolean expiration, Integer expirationWeeks, Boolean reuse, Integer reuseAmount,
      Boolean lock, Integer lockAttempts) {
    this.enabled = enabled;
    this.minLength = minLength;
    this.lowercase = lowercase;
    this.uppercase = uppercase;
    this.number = number;
    this.nonAlphanumeric = nonAlphanumeric;
    this.expiration = expiration;
    this.expirationWeeks = expirationWeeks;
    this.reuse = reuse;
    this.reuseAmount = reuseAmount;
    this.lock = lock;
    this.lockAttempts = lockAttempts;
  }
}
