package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum WebAuthnRequireResidentKey {
  UNSPECIFIED("unspecified"),
  TRUE("true"),
  FALSE("false");

  @Getter
  @JsonValue
  private final String value;

  WebAuthnRequireResidentKey(String value) {
    this.value = value;
  }
}
