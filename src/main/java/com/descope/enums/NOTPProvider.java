package com.descope.enums;

import lombok.Getter;

public enum NOTPProvider {
  WHATSAPP("whatsapp");

  @Getter
  private final String value;

  NOTPProvider(String value) {
    this.value = value;
  }
}
