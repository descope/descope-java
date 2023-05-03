package com.descope.literals;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Routes {

  @UtilityClass
  public static class AuthEndPoints {
    public static final String SIGN_IN_MAGIC_LINK = "/auth/magiclink/signin";
    public static final String SIGN_UP_MAGIC_LINK = "/auth/magiclink/signup";
    public static final String VERIFY_MAGIC_LINK = "/auth/magiclink/verify";
    public static final String SIGN_UP_OR_IN_MAGIC_LINK = "auth/magiclink/signup-in";
    public static final String UPDATE_EMAIL_MAGIC_LINK = "auth/magiclink/update/email";
    public static final String UPDATE_USER_PHONE_MAGIC_LINK = "auth/magiclink/update/phone";

    public static final String GET_KEYS_LINK = "/keys";
    public static final String REFRESH_TOKEN_LINK = "/auth/refresh";
  }
}
