package com.descope.exception;

import static com.descope.exception.ErrorCode.INVALID_TOKEN;

public class ClientFunctionalException extends DescopeException {

  protected ClientFunctionalException(String message, String code) {
    super(message);
    setCode(code);
  }

  protected ClientFunctionalException(String message, String code, Throwable cause) {
    super(message, cause);
    setCode(code);
  }

  public static ClientFunctionalException invalidToken() {
    String message = "Invalid Token";
    return new ClientFunctionalException(message, INVALID_TOKEN);
  }

  public static ClientFunctionalException invalidToken(Throwable cause) {
    String message = "Invalid Token";
    return new ClientFunctionalException(message, INVALID_TOKEN);
  }
}
