package com.descope.enums;

import lombok.Getter;

public enum DeliveryMethod {
  EMAIL("email"),
  SMS("sms"),
  VOICE("voice"),
  WHATSAPP("whatsapp");

  @Getter
  private final String value;

  DeliveryMethod(String value) {
    this.value = value;
  }
}
