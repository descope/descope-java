package com.descope.enums;

import lombok.Getter;

public enum DeliveryMethod {
  EMAIL("email"),
  SMS("sms"),
  WHATSAPP("whatsapp");

  @Getter private final String value;

  DeliveryMethod(String value) {
    this.value = value;
  }
}
