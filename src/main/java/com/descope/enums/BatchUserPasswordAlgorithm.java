package com.descope.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum BatchUserPasswordAlgorithm {
  BATCH_USER_PASSWORD_ALGORITHM_BCRYPT("bcrypt"),
  BATCH_USER_PASSWORD_ALGORITHM_PBKDF2SHA1("pbkdf2sha1"),
  BATCH_USER_PASSWORD_ALGORITHM_PBKDF2SHA256("pbkdf2sha256"),
  BATCH_USER_PASSWORD_ALGORITHM_PBKDF2SHA512("pbkdf2sha512");

  @Getter
  @JsonValue
  private final String value;

  BatchUserPasswordAlgorithm(String value) {
    this.value = value;
  }
}
