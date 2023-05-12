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
    public static final String EXCHANGE_ACCESS_KEY_LINK = "/auth/accesskey/exchange";
    public static final String SIGN_IN_OTP_LINK = "auth/otp/signin";
    public static final String SIGN_UP_OTP_LINK = "auth/otp/signup";
    public static final String SIGN_UP_OR_IN_OTP_LINK = "auth/otp/signup-in";
    public static final String VERIFY_CODE = "auth/otp/verify";
    public static final String OTP_UPDATE_EMAIL = "auth/otp/update/email";
    public static final String OTP_UPDATE_PHONE = "auth/otp/update/phone";
    public static final String SIGN_IN_ENCHANTED_LINK = "auth/enchantedlink/signin";
    public static final String SIGN_UP_ENCHANTED_LINK = "auth/enchantedlink/signup";
    public static final String SIGN_UP_OR_IN_ENCHANTED_LINK = "auth/enchantedlink/signup-in";
    public static final String VERIFY_ENCHANTED_LINK = "auth/enchantedlink/verify";
    public static final String ENCHANTED_LINK_SESSION = "auth/enchantedlink/pending-session";
    public static final String UPDATE_EMAIL_ENCHANTED_LINK = "auth/enchantedlink/update/email";
    public static final String TOTP_SIGNUP = "auth/totp/signup";
    public static final String TOTP_USERUPDATE = "auth/totp/update";
    public static final String VERIFY_TOTPCODE = "auth/totp/verify";
  }
}
