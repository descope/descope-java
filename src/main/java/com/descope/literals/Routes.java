package com.descope.literals;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Routes {

  @UtilityClass
  public static class AuthEndPoints {
    // Log out
    public static final String LOG_OUT_LINK = "/auth/logout";
    public static final String LOG_OUT_ALL_LINK = "/auth/logoutall";

    // MagicLink
    public static final String SIGN_IN_MAGIC_LINK = "/auth/magiclink/signin";
    public static final String SIGN_UP_MAGIC_LINK = "/auth/magiclink/signup";
    public static final String VERIFY_MAGIC_LINK = "/auth/magiclink/verify";
    public static final String SIGN_UP_OR_IN_MAGIC_LINK = "/auth/magiclink/signup-in";
    public static final String UPDATE_EMAIL_MAGIC_LINK = "/auth/magiclink/update/email";
    public static final String UPDATE_USER_PHONE_MAGIC_LINK = "/auth/magiclink/update/phone";

    // OTP
    public static final String SIGN_IN_OTP_LINK = "/auth/otp/signin";
    public static final String SIGN_UP_OTP_LINK = "/auth/otp/signup";
    public static final String SIGN_UP_OR_IN_OTP_LINK = "/auth/otp/signup-in";
    public static final String VERIFY_OTP_LINK = "/auth/otp/verify";
    public static final String OTP_UPDATE_EMAIL_LINK = "/auth/otp/update/email";
    public static final String OTP_UPDATE_PHONE_LINK = "/auth/otp/update/phone";

    // EnchantedLink
    public static final String SIGN_IN_ENCHANTED_LINK = "/auth/enchantedlink/signin";
    public static final String SIGN_UP_ENCHANTED_LINK = "/auth/enchantedlink/signup";
    public static final String SIGN_UP_OR_IN_ENCHANTED_LINK = "/auth/enchantedlink/signup-in";
    public static final String VERIFY_ENCHANTED_LINK = "/auth/enchantedlink/verify";
    public static final String ENCHANTED_LINK_SESSION = "/auth/enchantedlink/pending-session";
    public static final String UPDATE_EMAIL_ENCHANTED_LINK = "/auth/enchantedlink/update/email";

    // TOTP
    public static final String SIGN_UP_TOTP_LINK = "/auth/totp/signup";
    public static final String UPDATE_USER_TOTP_LINK = "/auth/totp/update";
    public static final String VERIFY_TOTP_LINK = "/auth/totp/verify";

    // OAuth
    public static final String COMPOSE_OAUTH_LINK = "/auth/oauth/authorize";
    public static final String EXCHANGE_OAUTH_LINK = "/auth/oauth/exchange";

    // SAML
    public static final String COMPOSE_SAML_START_LINK = "/auth/saml/authorize";
    public static final String EXCHANGE_SAML_LINK = "/auth/saml/exchange";

    // Password
    public static final String SIGN_UP_PASSWORD_LINK = "/auth/password/signup";
    public static final String SIGN_IN_PASSWORD_LINK = "/auth/password/signin";
    public static final String SEND_RESET_PASSWORD_LINK = "/auth/password/reset";
    public static final String UPDATE_USER_PASSWORD_LINK = "/auth/password/update";
    public static final String REPLACE_USER_PASSWORD_LINK = "/auth/password/replace";
    public static final String PASSWORD_POLICY_LINK = "/auth/password/policy";

    public static final String GET_KEYS_LINK = "/keys";
    public static final String REFRESH_TOKEN_LINK = "/auth/refresh";
    public static final String EXCHANGE_ACCESS_KEY_LINK = "/auth/accesskey/exchange";
  }

  @UtilityClass
  public static class ManagementEndPoints {
    // User
    public static final String CREATE_USER_LINK = "/v1/mgmt/user/create";
    public static final String UPDATE_USER_LINK = "/v1/mgmt/user/update";
    public static final String DELETE_USER_LINK = "/v1/mgmt/user/delete";
    public static final String DELETE_ALL_TEST_USERS_LINK = "/v1/mgmt/user/test/delete/all";
    public static final String LOAD_USER_LINK = "/v1/mgmt/user";
    public static final String USER_SEARCH_ALL_LINK = "/v1/mgmt/user/search";
    public static final String USER_UPDATE_STATUS_LINK = "/v1/mgmt/user/update/status";
    public static final String USER_UPDATE_EMAIL_LINK = "/v1/mgmt/user/update/email";
    public static final String USER_UPDATE_PHONE_LINK = "/v1/mgmt/user/update/phone";
    public static final String UPDATE_USER_NAME_LINK = "/v1/mgmt/user/update/name";
    public static final String UPDATE_PICTURE_LINK = "/v1/mgmt/user/update/picture";
    public static final String UPDATE_CUSTOM_ATTRIBUTE_LINK =
        "/v1/mgmt/user/update/customAttribute";
    public static final String USER_ADD_ROLES_LINK = "/v1/mgmt/user/update/role/add";
    public static final String USER_REMOVE_ROLES_LINK = "/v1/mgmt/user/update/role/remove";
    public static final String USER_ADD_TENANT_LINK = "/v1/mgmt/user/update/tenant/add";
    public static final String USER_REMOVE_TENANT_LINK = "/v1/mgmt/user/update/tenant/remove";
    public static final String COMPOSE_OTP_FOR_TEST_LINK = "/v1/mgmt/tests/generate/otp";
    public static final String MAGIC_LINK_FOR_TEST_LINK = "/v1/mgmt/tests/generate/magiclink";
    public static final String ENCHANTED_LINK_FOR_TEST_LINK =
        "/v1/mgmt/tests/generate/enchantedlink";
    public static final String USER_SET_PASSWORD_LINK = "/v1/mgmt/user/password/set";
    public static final String USER_EXPIRE_PASSWORD_LINK = "/v1/mgmt/user/password/expire";

    // Tenant
    public static final String CREATE_TENANT_LINK = "/v1/mgmt/tenant/create";
    public static final String UPDATE_TENANT_LINK = "/v1/mgmt/tenant/update";
    public static final String DELETE_TENANT_LINK = "/v1/mgmt/tenant/delete";
    public static final String LOAD_ALL_TENANTS_LINK = "/v1/mgmt/tenant/all";

    // SSO
    public static final String SSO_GET_SETTINGS_LINK = "/mgmt/sso/settings";
    public static final String SSO_DELETE_SETTINGS_LINK = "/mgmt/sso/settings";
    public static final String SSO_CONFIGURE_SETTINGS_LINK = "/mgmt/sso/settings";
    public static final String SSO_CONFIGURE_METADATA_LINK = "/mgmt/sso/metadata";
    public static final String SSO_CONFIGURE_MAPPING_LINK = "/mgmt/sso/mapping";

    // Group
    public static final String GROUP_LOAD_ALL_LINK = "/v1/mgmt/group/all";
    public static final String LOAD_ALL_FOR_GROUP_MEMBERS_LINK = "/v1/mgmt/group/member/all";
    public static final String LOAD_ALL_GROUP_MEMBERS_LINK = "/v1/mgmt/group/members";

    // FLOW
    public static final String FLOW_EXPORT_LINK = "/v1/mgmt/flow/export";
    public static final String FLOW_IMPORT_LINK = "/v1/mgmt/flow/import";
    public static final String THEME_EXPORT_LINK = "/v1/mgmt/theme/export";
    public static final String THEME_IMPORT_LINK = "/v1/mgmt/theme/import";

    // JWT
    public static final String UPDATE_JWT_LINK = "/v1/mgmt/jwt/update";

    // Access key
    public static final String MANAGEMENT_ACCESS_KEY_CREATE_LINK = "/v1/mgmt/accesskey/create";
    public static final String MANAGEMENT_ACCESS_KEY_LOAD_LINK = "/v1/mgmt/accesskey";
    public static final String MANAGEMENT_ACCESS_KEY_SEARCH_ALL_LINK = "/v1/mgmt/accesskey/search";
    public static final String MANAGEMENT_ACCESS_KEY_UPDATE_LINK = "/v1/mgmt/accesskey/update";
    public static final String MANAGEMENT_ACCESS_KEY_DEACTIVATE_LINK =
        "/v1/mgmt/accesskey/deactivate";
    public static final String MANAGEMENT_ACCESS_KEY_ACTIVE_LINK = "/v1/mgmt/accesskey/activate";
    public static final String MANAGEMENT_ACCESS_KEY_DELETE_LINK = "/v1/mgmt/accesskey/delete";

    // Permission
    public static final String MANAGEMENT_PERMISSION_CREATE_LINK = "/v1/mgmt/permission/create";
    public static final String MANAGEMENT_PERMISSION_UPDATE_LINK = "/v1/mgmt/permission/update";
    public static final String MANAGEMENT_PERMISSION_DELETE_LINK = "/v1/mgmt/permission/delete";
    public static final String MANAGEMENT_PERMISSION_LOAD_ALL_LINK = "/v1/mgmt/permission/all";

    // Roles
    public static final String MANAGEMENT_ROLES_CREATE_LINK = "/v1/mgmt/role/create";
    public static final String MANAGEMENT_ROLES_UPDATE_LINK = "/v1/mgmt/role/update";
    public static final String MANAGEMENT_ROLES_DELETE_LINK = "/v1/mgmt/role/delete";
    public static final String MANAGEMENT_ROLES_LOAD_ALL_LINK = "/v1/mgmt/role/all";

    // Audit
    public static final String MANAGEMENT_AUDIT_SEARCH_LINK = "/v1/mgmt/audit/search";
  }
}
