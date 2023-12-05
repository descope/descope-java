package com.descope.exception;

import static com.descope.exception.ErrorCode.ERR_MISSING_ARGUMENTS;
import static com.descope.exception.ErrorCode.ERR_REFRESH_TOKEN;
import static com.descope.exception.ErrorCode.INVALID_ARGUMENT;
import static com.descope.exception.ErrorCode.INVALID_SIGNING_KEY;

import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true, includeFieldNames = true)
@Getter
public class ServerCommonException extends DescopeException {

  private String serverResponse;

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

  public static ServerCommonException invalidSigningKey(String error) {
    return new ServerCommonException(error, INVALID_SIGNING_KEY);
  }

  public static ServerCommonException genericServerError(String message, String code, String serverResponse) {
    ServerCommonException e = new ServerCommonException(message, code);
    e.serverResponse = serverResponse;
    return e;
  }
}
