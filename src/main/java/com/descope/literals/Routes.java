package com.descope.literals;

import lombok.experimental.UtilityClass;

public class Routes {

  @UtilityClass
  public static class AuthEndPoints {
    public static final String SIGN_IN_MAGIC_LINK = "/auth/magiclink/signin";
    public static final String SIGN_UP_MAGIC_LINK = "/auth/magiclink/signup";
    public static final String VERIFY_MAGIC_LINK = "/auth/magiclink/verify";

    public static final String GET_KEYS_LINK = "/keys";
  }
}
