package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum UserStatus {
  ENABLED("enabled"),
  DISABLED("disabled"),
  INVITED("invited");

  @Getter
  @JsonValue
  private final String value;

  UserStatus(String value) {
    this.value = value;
  }
}
