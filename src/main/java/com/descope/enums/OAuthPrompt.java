package com.descope.enums;

import lombok.Getter;

public enum OAuthPrompt {
  NONE("none"),
  LOGIN("login"),
  CONSENT("consent"),
  SELECT_ACCOUNT("select_account");

  @Getter
  private final String value;

  OAuthPrompt(String value) {
    this.value = value;
  }
}
