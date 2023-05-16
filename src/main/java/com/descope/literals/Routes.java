package com.descope.literals;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Routes {

  @UtilityClass
  public static class AuthEndPoints {
    // MagicLink
    public static final String SIGN_IN_MAGIC_LINK = "/auth/magiclink/signin";
    public static final String SIGN_UP_MAGIC_LINK = "/auth/magiclink/signup";
    public static final String VERIFY_MAGIC_LINK = "/auth/magiclink/verify";
    public static final String SIGN_UP_OR_IN_MAGIC_LINK = "auth/magiclink/signup-in";
    public static final String UPDATE_EMAIL_MAGIC_LINK = "auth/magiclink/update/email";
    public static final String UPDATE_USER_PHONE_MAGIC_LINK = "auth/magiclink/update/phone";

    // OTP
    public static final String SIGN_IN_OTP_LINK = "auth/otp/signin";
    public static final String SIGN_UP_OTP_LINK = "auth/otp/signup";
    public static final String SIGN_UP_OR_IN_OTP_LINK = "auth/otp/signup-in";
    public static final String VERIFY_OTP_LINK = "auth/otp/verify";
    public static final String OTP_UPDATE_EMAIL_LINK = "auth/otp/update/email";
    public static final String OTP_UPDATE_PHONE_LINK = "auth/otp/update/phone";

    // EnchantedLink
    public static final String SIGN_IN_ENCHANTED_LINK = "auth/enchantedlink/signin";
    public static final String SIGN_UP_ENCHANTED_LINK = "auth/enchantedlink/signup";
    public static final String SIGN_UP_OR_IN_ENCHANTED_LINK = "auth/enchantedlink/signup-in";
    public static final String VERIFY_ENCHANTED_LINK = "auth/enchantedlink/verify";
    public static final String ENCHANTED_LINK_SESSION = "auth/enchantedlink/pending-session";
    public static final String UPDATE_EMAIL_ENCHANTED_LINK = "auth/enchantedlink/update/email";

    // TOTP
    public static final String SIGN_UP_TOTP_LINK = "auth/totp/signup";
    public static final String UPDATE_USER_TOTP_LINK = "auth/totp/update";
    public static final String VERIFY_TOTP_LINK = "auth/totp/verify";

    public static final String GET_KEYS_LINK = "/keys";
    public static final String REFRESH_TOKEN_LINK = "/auth/refresh";
    public static final String EXCHANGE_ACCESS_KEY_LINK = "/auth/accesskey/exchange";
  }

  @UtilityClass
  public static class ManagementEndPoints {
    // User
    public static final String CREATE_USER_LINK = "/mgmt/user/create";
    public static final String UPDATE_USER_LINK = "/mgmt/user/update";
    public static final String DELETE_USER_LINK = "/mgmt/user/delete";
    public static final String DELETE_ALL_TEST_USERS_LINK = "mgmt/user/test/delete/all";
    public static final String LOAD_USER_LINK = "mgmt/user";
    public static final String USER_SEARCH_ALL = "mgmt/user/search";

    // Tenant
    public static final String CREATE_TENANT_LINK = "/mgmt/tenant/create";
    public static final String UPDATE_TENANT_LINK = "/mgmt/tenant/update";
    public static final String DELETE_TENANT_LINK = "/mgmt/tenant/delete";
    public static final String LOAD_ALL_TENANTS_LINK = "/mgmt/tenant/all";

    // JWT
    public static final String UPDATE_JWT_LINK = "/mgmt/jwt/update";
  }
}
