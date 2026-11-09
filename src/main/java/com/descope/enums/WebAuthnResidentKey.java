package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum WebAuthnResidentKey {
  DISCOURAGED("discouraged"),
  PREFERRED("preferred"),
  REQUIRED("required");

  @Getter
  @JsonValue
  private final String value;

  WebAuthnResidentKey(String value) {
    this.value = value;
  }
}
