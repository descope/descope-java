package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum TenantRoleInheritance {
  NONE("none"),
  USER_ONLY("userOnly");

  @Getter
  @JsonValue
  private final String value;

  TenantRoleInheritance(String value) {
    this.value = value;
  }
}
