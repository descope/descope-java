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
    //oauth
    public static final String COMPOSE_OAUTH_LINK = "auth/oauth/authorize";
    public static final String EXCHANGE_OAUTH_LINK = "auth/oauth/exchange";
    //SAML
    public static final String COMPOSE_SAML_START_LINK = "auth/saml/authorize";
    public static final String EXCHANGE_SAML_LINK = "auth/saml/exchange";

    //Password
    public static final String SIGNUP_PASSWORD = "auth/password/signup";
    public static final String SIGNIN_PASSWORD = "auth/password/signin";
    public static final String SEND_RESET_PASSWORD = "auth/password/reset";
    public static final String UPDATE_USER_PASSWORD = "auth/password/update";
    public static final String REPLACE_USER_PASSWORD = "auth/password/replace";
    public static final String PASSWORD_POLICY = "auth/password/policy";

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
    public static final String USER_UPDATE_STATUS_LINK = "mgmt/user/update/status";
    public static final String USER_UPDATE_EMAIL_LINK = "mgmt/user/update/email";
    public static final String USER_UPDATE_PHONE_LINK = "mgmt/user/update/phone";
    public static final String UPDATE_USER_NAME = "mgmt/user/update/name";
    public static final String UPDATE_PICTURE_LINK = "mgmt/user/update/picture";
    public static final String UPDATE_CUSTOM_ATTRIBUTE_LINK = "mgmt/user/update/customAttribute";
    public static final String USER_ADD_ROLES = "mgmt/user/update/role/add";
    public static final String USER_REMOVE_ROLES = "mgmt/user/update/role/remove";
    public static final String USER_ADD_TENANT = "mgmt/user/update/tenant/add";
    public static final String USER_REMOVE_TENANT = "mgmt/user/update/tenant/remove";
    public static final String COMPOSE_OTP_FOR_TEST = "mgmt/tests/generate/otp";
    public static final String MAGIC_LINK_FOR_TEST = "mgmt/tests/generate/magiclink";
    public static final String ENCHANTED_LINK_FOR_TEST = "mgmt/tests/generate/enchantedlink";
    public static final String USER_SET_PASSWORD = "mgmt/user/password/set";
    public static final String USER_EXPIRE_PASSWORD = "mgmt/user/password/expire";
    // Tenant
    public static final String CREATE_TENANT_LINK = "/mgmt/tenant/create";
    public static final String UPDATE_TENANT_LINK = "/mgmt/tenant/update";
    public static final String DELETE_TENANT_LINK = "/mgmt/tenant/delete";
    public static final String LOAD_ALL_TENANTS_LINK = "/mgmt/tenant/all";

    // JWT
    public static final String UPDATE_JWT_LINK = "/mgmt/jwt/update";

    //Access key
    public static final String MANAGEMENT_ACCESSKEY_CREATE = "mgmt/accesskey/create";

    public static final String MANAGEMENT_ACCESSKEY_LOAD = "mgmt/accesskey";
    public static final String MANAGEMENT_ACCESSKEY_SEARCH_ALL = "mgmt/accesskey/search";
    public static final String MANAGEMENT_ACCESSKEY_UPDATE = "mgmt/accesskey/update";
    public static final String MANAGEMENT_ACCESSKEY_DEACTIVE = "mgmt/accesskey/deactivate";
    public static final String MANAGEMENT_ACCESSKEY_ACTIVE = "mgmt/accesskey/activate";
    public static final String MANAGEMENT_ACCESSKEY_DELETE = "mgmt/accesskey/delete";
  }
}
