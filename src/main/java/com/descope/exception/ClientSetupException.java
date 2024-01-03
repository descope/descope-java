package com.descope.exception;

import static com.descope.exception.ErrorCode.INVALID_PROJECT_ID;
import static com.descope.exception.ErrorCode.MISSING_PROJECT_ID;

public class ClientSetupException extends DescopeException {

  protected ClientSetupException(String message, String code) {
    super(message);
    setCode(code);
  }

  public static ClientSetupException missingProjectId() {
    String message = "Missing project ID";
    return new ClientSetupException(message, MISSING_PROJECT_ID);
  }

  public static ClientSetupException invliadProjectId() {
    String message = "Invalid project ID - must be over 27 characters long";
    return new ClientSetupException(message, INVALID_PROJECT_ID);
  }
}
