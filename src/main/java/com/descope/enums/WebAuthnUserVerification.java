package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum WebAuthnUserVerification {
  PREFERRED("preferred"),
  REQUIRED("required"),
  DISCOURAGED("discouraged");

  @Getter
  @JsonValue
  private final String value;

  WebAuthnUserVerification(String value) {
    this.value = value;
  }
}
