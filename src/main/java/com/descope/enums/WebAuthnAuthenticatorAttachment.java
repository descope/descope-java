package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum WebAuthnAuthenticatorAttachment {
  ANY("any"),
  PLATFORM("platform"),
  CROSS_PLATFORM("crossplatform");

  @Getter
  @JsonValue
  private final String value;

  WebAuthnAuthenticatorAttachment(String value) {
    this.value = value;
  }
}
