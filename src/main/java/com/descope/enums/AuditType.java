package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum AuditType {
  INFO("info"),
  WARN("warn"),
  ERROR("error");

  @Getter
  @JsonValue
  private final String value;

  AuditType(String value) {
    this.value = value;
  }

  public static AuditType fromString(String value) {
    if (ERROR.value.equalsIgnoreCase(value)) {
      return ERROR;
    }
    if (WARN.value.equalsIgnoreCase(value)) {
      return WARN;
    }
    return INFO;
  }
}
