package com.descope.proxy.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
  private String errorCode;
  private String errorDescription;
  private String errorMessage;
  private String message;

  public String getActualMessage() {
    return errorMessage == null
      ? message == null ? errorDescription : message
      : errorMessage;
  }
}
