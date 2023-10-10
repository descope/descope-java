package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum NodeType {
  CHILD("child"),
  UNION("union"),
  INTERSECT("intersect"),
  SUB("sub");

  @Getter
  @JsonValue
  private final String value;

  NodeType(String value) {
    this.value = value;
  }
}
