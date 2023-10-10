package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum NodeExpressionType {
  SELF("self"),
  TARGET_SET("targetSet"),
  RELATION_LEFT("relationLeft"),
  RELATION_RIGHT("relationRight");

  @Getter
  @JsonValue
  private final String value;

  NodeExpressionType(String value) {
    this.value = value;
  }
}
