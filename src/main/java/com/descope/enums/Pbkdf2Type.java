package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

public enum Pbkdf2Type {
  SHA1("sha1"),
  SHA256("sha256"),
  SHA512("sha512");

  @Getter
  @JsonValue
  private final String value;

  Pbkdf2Type(String value) {
    this.value = value;
  }
}
