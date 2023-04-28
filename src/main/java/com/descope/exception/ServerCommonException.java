package com.descope.exception;

import static com.descope.exception.ErrorCode.*;

public class ServerCommonException extends DescopeException {

  protected ServerCommonException(String message, String code) {
    super(message);
    setCode(code);
  }

  public static ServerCommonException invalidArgument(String property) {
    String message = String.format("The %s argument is invalid", property);
    return new ServerCommonException(message, INVALID_ARGUMENT);
  }

  public static ServerCommonException errorRefreshToken(String error) {
    return new ServerCommonException(error, ERR_REFRESH_TOKEN);
  }

  public static ServerCommonException errMissingArguments(String error) {
    return new ServerCommonException(error, ERR_MISSING_ARGUMENTS);
  }

}