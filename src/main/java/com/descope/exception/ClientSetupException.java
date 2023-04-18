package com.descope.exception;

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
}
