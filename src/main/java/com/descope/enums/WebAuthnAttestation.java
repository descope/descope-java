package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum WebAuthnAttestation {
  NONE("none"),
  INDIRECT("indirect"),
  DIRECT("direct");

  @Getter
  @JsonValue
  private final String value;

  WebAuthnAttestation(String value) {
    this.value = value;
  }
}
