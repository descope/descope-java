package com.descope.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCode {

  public static final String INTERNAL_SERVER_ERROR = "500";

  // server common
  public static final String INVALID_ARGUMENT = "E011004";

  // client setup
  public static final String MISSING_PROJECT_ID = "G010001";

  // client functional errors
  public static final String INVALID_TOKEN = "G030002";
}
