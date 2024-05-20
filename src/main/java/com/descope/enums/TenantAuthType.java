package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum TenantAuthType {
  OIDC("oidc"),
  SAML("saml"),
  NONE("none");

  @Getter
  @JsonValue
  private final String value;

  TenantAuthType(String value) {
    this.value = value;
  }
}
