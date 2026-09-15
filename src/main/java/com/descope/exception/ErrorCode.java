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
  public static final String INVALID_PROJECT_ID = "G010002";

  // client functional errors
  public static final String INVALID_TOKEN = "G030002";
  public static final String ERR_MISSING_ARGUMENTS = "E011002";

  // rate limit
  public static final String RATE_LIMIT_EXCEEDED = "E130429";

  // conflicts - the new identifier already belongs to another user and the caller asked to
  // fail instead of letting the server merge the two users
  public static final String USER_UPDATE_CONFLICT = "E111127";
  public static final String AUTH_USER_UPDATE_CONFLICT = "E062125";

  // other user errors
  public static final String USER_ALREADY_EXISTS = "E062107";
  public static final String USER_NOT_FOUND = "E112102";

  // server common
  public static final String BAD_REQUEST = "E011001";
  public static final String VALIDATION_FAILURE = "E011003";

  // No keys
  public static final String INVALID_SIGNING_KEY = "J010001";
}
