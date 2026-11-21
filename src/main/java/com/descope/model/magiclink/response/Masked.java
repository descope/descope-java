package com.descope.model.magiclink.response;

public interface Masked {
  String getMasked();

  /** Number of verification attempts made so far, when the response has it. */
  default Integer getVerificationAttempts() {
    return null;
  }
}
