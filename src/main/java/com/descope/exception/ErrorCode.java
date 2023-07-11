package com.descope.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCode {

  public static final String INTERNAL_SERVER_ERROR = "500";

  // server common
  public static final String INVALID_ARGUMENT = "E011004";
  public static final String ERR_REFRESH_TOKEN = "G030003";

  // client setup
  public static final String MISSING_PROJECT_ID = "G010001";

  // client functional errors
  public static final String INVALID_TOKEN = "G030002";
  public static final String ERR_MISSING_ARGUMENTS = "E011002";

  // rate limit
  public static final String RATE_LIMIT_EXCEEDED = "E130429";
}
