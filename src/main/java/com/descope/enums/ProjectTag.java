package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum ProjectTag {
	None(""),
  Production("production");

  @Getter
  @JsonValue
  private final String value;

  ProjectTag(String value) {
    this.value = value;
  }
}
