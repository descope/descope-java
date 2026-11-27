package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum PromptType {
  NONE("none"),
  LOGIN("login"),
  CONSENT("consent"),
  SELECT_ACCOUNT("select_account");

  @Getter
  @JsonValue
  private final String value;

  PromptType(String value) {
    this.value = value;
  }
}
