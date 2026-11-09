package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum FGACheckPathStepType {
  UNKNOWN("UNKNOWN"),
  RELATION("RELATION"),
  INTERSECTION("INTERSECTION");

  @Getter
  @JsonValue
  private final String value;

  FGACheckPathStepType(String value) {
    this.value = value;
  }

  // Unrecognized values from newer servers map to UNKNOWN
  @JsonCreator
  public static FGACheckPathStepType fromValue(String value) {
    for (FGACheckPathStepType t : values()) {
      if (t.value.equals(value)) {
        return t;
      }
    }
    return UNKNOWN;
  }
}
