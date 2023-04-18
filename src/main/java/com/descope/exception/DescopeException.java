package com.descope.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class DescopeException extends RuntimeException {

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String code = ErrorCode.INTERNAL_SERVER_ERROR;

  protected DescopeException(String message) {
    super(message);
  }

  protected DescopeException(String message, Throwable cause) {
    super(message, cause);
  }

  protected DescopeException(Throwable cause) {
    super(cause);
  }
}
