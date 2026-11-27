package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ProjectEnvironment {
  Production("production");

  @Getter
  @JsonValue
  private final String value;

  ProjectEnvironment(String value) {
    this.value = value;
  }
}
