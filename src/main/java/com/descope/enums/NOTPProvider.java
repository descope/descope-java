package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum NOTPProvider {
  WHATSAPP("whatsapp");

  @Getter
  @JsonValue
  private final String value;

  NOTPProvider(String value) {
    this.value = value;
  }
}
