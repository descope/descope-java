package com.descope.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends DescopeException {
  private long retryAfterSeconds;

  public RateLimitExceededException(String message, String code, long retryAfterSeconds) {
    super(message);
    setCode(code);
    this.retryAfterSeconds = retryAfterSeconds;
  }

}
