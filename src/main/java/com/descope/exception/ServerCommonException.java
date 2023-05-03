package com.descope.exception;

import static com.descope.exception.ErrorCode.ERR_MISSING_ARGUMENTS;
import static com.descope.exception.ErrorCode.ERR_REFRESH_TOKEN;
import static com.descope.exception.ErrorCode.INVALID_ARGUMENT;

public class ServerCommonException extends DescopeException {

  protected ServerCommonException(String message, String code) {
    super(message);
    setCode(code);
  }

  public static ServerCommonException invalidArgument(String property) {
    String message = String.format("The %s argument is invalid", property);
    return new ServerCommonException(message, INVALID_ARGUMENT);
  }

  public static ServerCommonException refreshToken(String error) {
    return new ServerCommonException(error, ERR_REFRESH_TOKEN);
  }

  public static ServerCommonException missingArguments(String error) {
    return new ServerCommonException(error, ERR_MISSING_ARGUMENTS);
  }
}
