package com.descope.literals;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Routes {

  @UtilityClass
  public static class AuthEndPoints {
    // Log out
    public static final String LOG_OUT_LINK = "/v1/auth/logout";
    public static final String LOG_OUT_ALL_LINK = "/v1/auth/logoutall";

    // My details
    public static final String ME_LINK = "/v1/auth/me";
    public static final String HISTORY_LINK = "/v1/auth/me/history";

    // MagicLink
    public static final String SIGN_IN_MAGIC_LINK = "/v1/auth/magiclink/signin";
    public static final String SIGN_UP_MAGIC_LINK = "/v1/auth/magiclink/signup";
    public static final String VERIFY_MAGIC_LINK = "/v1/auth/magiclink/verify";
    public static final String SIGN_UP_OR_IN_MAGIC_LINK = "/v1/auth/magiclink/signup-in";
    public static final String UPDATE_EMAIL_MAGIC_LINK = "/v1/auth/magiclink/update/email";
    public static final String UPDATE_USER_PHONE_MAGIC_LINK = "/v1/auth/magiclink/update/phone";

    // OTP
    public static final String SIGN_IN_OTP_LINK = "/v1/auth/otp/signin";
    public static final String SIGN_UP_OTP_LINK = "/v1/auth/otp/signup";
    public static final String SIGN_UP_OR_IN_OTP_LINK = "/v1/auth/otp/signup-in";
    public static final String VERIFY_OTP_LINK = "/v1/auth/otp/verify";
    public static final String OTP_UPDATE_EMAIL_LINK = "/v1/auth/otp/update/email";
    public static final String OTP_UPDATE_PHONE_LINK = "/v1/auth/otp/update/phone";

    // EnchantedLink
    public static final String SIGN_IN_ENCHANTED_LINK = "/v1/auth/enchantedlink/signin";
    public static final String SIGN_UP_ENCHANTED_LINK = "/v1/auth/enchantedlink/signup";
    public static final String SIGN_UP_OR_IN_ENCHANTED_LINK = "/v1/auth/enchantedlink/signup-in";
    public static final String VERIFY_ENCHANTED_LINK = "/v1/auth/enchantedlink/verify";
    public static final String ENCHANTED_LINK_SESSION = "/v1/auth/enchantedlink/pending-session";
    public static final String UPDATE_EMAIL_ENCHANTED_LINK = "/v1/auth/enchantedlink/update/email";

    // TOTP
    public static final String SIGN_UP_TOTP_LINK = "/v1/auth/totp/signup";
    public static final String UPDATE_USER_TOTP_LINK = "/v1/auth/totp/update";
    public static final String VERIFY_TOTP_LINK = "/v1/auth/totp/verify";

    // OAuth
    public static final String COMPOSE_OAUTH_LINK = "/v1/auth/oauth/authorize";
    public static final String COMPOSE_OAUTH_LINK_SIGN_IN = "/v1/auth/oauth/authorize/signin";
    public static final String COMPOSE_OAUTH_LINK_SIGN_UP = "/v1/auth/oauth/authorize/signup";
    public static final String EXCHANGE_OAUTH_LINK = "/v1/auth/oauth/exchange";

    // SAML
    public static final String COMPOSE_SAML_START_LINK = "/v1/auth/saml/authorize";
    public static final String EXCHANGE_SAML_LINK = "/v1/auth/saml/exchange";

    // SSO
    public static final String COMPOSE_SSO_START_LINK = "/v1/auth/sso/authorize";
    public static final String EXCHANGE_SSO_LINK = "/v1/auth/sso/exchange";

    // Password
    public static final String SIGN_UP_PASSWORD_LINK = "/v1/auth/password/signup";
    public static final String SIGN_IN_PASSWORD_LINK = "/v1/auth/password/signin";
    public static final String SEND_RESET_PASSWORD_LINK = "/v1/auth/password/reset";
    public static final String UPDATE_USER_PASSWORD_LINK = "/v1/auth/password/update";
    public static final String REPLACE_USER_PASSWORD_LINK = "/v1/auth/password/replace";
    public static final String PASSWORD_POLICY_LINK = "/v1/auth/password/policy";

    // WebAuthn
    public static final String WEBAUTHN_SIGN_UP_START = "/v1/auth/webauthn/signup/start";
    public static final String WEBAUTHN_SIGN_UP_FINISH = "/v1/auth/webauthn/signup/finish";
    public static final String WEBAUTHN_SIGN_IN_START = "/v1/auth/webauthn/signin/start";
    public static final String WEBAUTHN_SIGN_IN_FINISH = "/v1/auth/webauthn/signin/finish";
    public static final String WEBAUTHN_SIGN_UP_OR_IN_START = "/v1/auth/webauthn/signup-in/start";
    public static final String WEBAUTHN_UPDATE_START = "/v1/auth/webauthn/update/start";
    public static final String WEBAUTHN_UPDATE_FINISH = "/v1/auth/webauthn/update/finish";

    public static final String GET_KEYS_LINK = "/v2/keys";
    public static final String REFRESH_TOKEN_LINK = "/v1/auth/refresh";
    public static final String EXCHANGE_ACCESS_KEY_LINK = "/v1/auth/accesskey/exchange";
  }

  @UtilityClass
  public static class ManagementEndPoints {
    // User
    public static final String CREATE_USER_LINK = "/v1/mgmt/user/create";
    public static final String CREATE_USERS_BATCH_LINK = "/v1/mgmt/user/create/batch";
    public static final String UPDATE_USER_LINK = "/v1/mgmt/user/update";
    public static final String DELETE_USER_LINK = "/v1/mgmt/user/delete";
    public static final String DELETE_ALL_TEST_USERS_LINK = "/v1/mgmt/user/test/delete/all";
    public static final String LOAD_USER_LINK = "/v1/mgmt/user";
    public static final String LOGOUT_USER_LINK = "/v1/mgmt/user/logout";
    public static final String USER_SEARCH_ALL_LINK = "/v1/mgmt/user/search";
    public static final String USER_UPDATE_STATUS_LINK = "/v1/mgmt/user/update/status";
    public static final String USER_UPDATE_EMAIL_LINK = "/v1/mgmt/user/update/email";
    public static final String USER_UPDATE_PHONE_LINK = "/v1/mgmt/user/update/phone";
    public static final String UPDATE_USER_NAME_LINK = "/v1/mgmt/user/update/name";
    public static final String UPDATE_PICTURE_LINK = "/v1/mgmt/user/update/picture";
    public static final String UPDATE_CUSTOM_ATTRIBUTE_LINK = "/v1/mgmt/user/update/customAttribute";
    public static final String UPDATE_USER_LOGIN_ID_LINK = "/v1/mgmt/user/update/loginid";
    public static final String USER_SET_ROLES_LINK = "/v1/mgmt/user/update/role/set";
    public static final String USER_ADD_ROLES_LINK = "/v1/mgmt/user/update/role/add";
    public static final String USER_REMOVE_ROLES_LINK = "/v1/mgmt/user/update/role/remove";
    public static final String USER_SET_SSO_APPS_LINK = "/v1/mgmt/user/update/ssoapp/set";
    public static final String USER_ADD_SSO_APPS_LINK = "/v1/mgmt/user/update/ssoapp/add";
    public static final String USER_REMOVE_SSO_APPS_LINK = "/v1/mgmt/user/update/ssoapp/remove";
    public static final String USER_ADD_TENANT_LINK = "/v1/mgmt/user/update/tenant/add";
    public static final String USER_REMOVE_TENANT_LINK = "/v1/mgmt/user/update/tenant/remove";
    public static final String GET_PROVIDER_TOKEN = "/v1/mgmt/user/provider/token";
    public static final String COMPOSE_OTP_FOR_TEST_LINK = "/v1/mgmt/tests/generate/otp";
    public static final String MAGIC_LINK_FOR_TEST_LINK = "/v1/mgmt/tests/generate/magiclink";
    public static final String ENCHANTED_LINK_FOR_TEST_LINK = "/v1/mgmt/tests/generate/enchantedlink";
    public static final String USER_SET_ACTIVE_PASSWORD_LINK = "/v1/mgmt/user/password/set/active";
    public static final String USER_SET_PASSWORD_LINK = "/v1/mgmt/user/password/set";
    public static final String USER_SET_TEMPORARY_PASSWORD_LINK = "/v1/mgmt/user/password/set/temporary";
    public static final String USER_EXPIRE_PASSWORD_LINK = "/v1/mgmt/user/password/expire";
    public static final String USER_CREATE_EMBEDDED_LINK = "/v1/mgmt/user/signin/embeddedlink";
    public static final String USER_HISTORY_LINK = "/v1/mgmt/user/history";

    // Tenant
    public static final String CREATE_TENANT_LINK = "/v1/mgmt/tenant/create";
    public static final String UPDATE_TENANT_LINK = "/v1/mgmt/tenant/update";
    public static final String DELETE_TENANT_LINK = "/v1/mgmt/tenant/delete";
    public static final String LOAD_TENANT_LINK = "/v1/mgmt/tenant";
    public static final String LOAD_ALL_TENANTS_LINK = "/v1/mgmt/tenant/all";
    public static final String TENANT_SEARCH_ALL_LINK = "/v1/mgmt/tenant/search";
    public static final String GET_TENANT_SETTINGS_LINK = "/v1/mgmt/tenant/settings";
    
    // SSO
    public static final String SSO_GET_SETTINGS_LINK = "/v1/mgmt/sso/settings";
    public static final String SSO_DELETE_SETTINGS_LINK = "/v1/mgmt/sso/settings";
    public static final String SSO_CONFIGURE_SETTINGS_LINK = "/v1/mgmt/sso/settings";
    public static final String SSO_CONFIGURE_METADATA_LINK = "/v1/mgmt/sso/metadata";
    public static final String SSO_CONFIGURE_MAPPING_LINK = "/v1/mgmt/sso/mapping";
    public static final String SSO_GET_SETTINGS_V2_LINK = "/v2/mgmt/sso/settings";
    public static final String SSO_CONFIGURE_SAML_SETTINGS_LINK = "/v1/mgmt/sso/saml";
    public static final String SSO_CONFIGURE_SAML_SETTINGS_BY_MD_LINK = "/v1/mgmt/sso/saml/metadata";
    public static final String SSO_CONFIGURE_OIDC_SETTINGS_LINK = "/v1/mgmt/sso/oidc";

    // SSO Application
    public static final String SSO_APPLICATION_OIDC_CREATE_LINK = "/v1/mgmt/sso/idp/app/oidc/create";
    public static final String SSO_APPLICATION_SAML_CREATE_LINK = "/v1/mgmt/sso/idp/app/saml/create";
    public static final String SSO_APPLICATION_OIDC_UPDATE_LINK = "/v1/mgmt/sso/idp/app/oidc/update";
    public static final String SSO_APPLICATION_SAML_UPDATE_LINK = "/v1/mgmt/sso/idp/app/saml/update";
    public static final String SSO_APPLICATION_DELETE_LINK = "/v1/mgmt/sso/idp/app/delete";
    public static final String SSO_APPLICATION_LOAD_LINK = "/v1/mgmt/sso/idp/app/load";
    public static final String SSO_APPLICATION_LOAD_ALL_LINK = "/v1/mgmt/sso/idp/apps/load";

    // Group
    public static final String GROUP_LOAD_ALL_LINK = "/v1/mgmt/group/all";
    public static final String LOAD_ALL_FOR_GROUP_MEMBERS_LINK = "/v1/mgmt/group/member/all";
    public static final String LOAD_ALL_GROUP_MEMBERS_LINK = "/v1/mgmt/group/members";

    // FLOW
    public static final String FLOW_LIST_LINK = "/v1/mgmt/flow/list";
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
    public static final String MANAGEMENT_ACCESS_KEY_DEACTIVATE_LINK = "/v1/mgmt/accesskey/deactivate";
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
    public static final String MANAGEMENT_ROLES_SEARCH_LINK = "/v1/mgmt/role/search";

    // Project
    public static final String MANAGEMENT_PROJECT_UPDATE_NAME = "/v1/mgmt/project/update/name";
    public static final String MANAGEMENT_PROJECT_CLONE = "/v1/mgmt/project/clone";
    public static final String MANAGEMENT_PROJECT_EXPORT = "/v1/mgmt/project/export";
    public static final String MANAGEMENT_PROJECT_IMPORT = "/v1/mgmt/project/import";

    // Audit
    public static final String MANAGEMENT_AUDIT_SEARCH_LINK = "/v1/mgmt/audit/search";

    // Authz
    public static final String MANAGEMENT_AUTHZ_SCHEMA_SAVE = "/v1/mgmt/authz/schema/save";
    public static final String MANAGEMENT_AUTHZ_SCHEMA_DELETE = "/v1/mgmt/authz/schema/delete";
    public static final String MANAGEMENT_AUTHZ_SCHEMA_LOAD = "/v1/mgmt/authz/schema/load";
    public static final String MANAGEMENT_AUTHZ_NS_SAVE = "/v1/mgmt/authz/ns/save";
    public static final String MANAGEMENT_AUTHZ_NS_DELETE = "/v1/mgmt/authz/ns/delete";
    public static final String MANAGEMENT_AUTHZ_RD_SAVE = "/v1/mgmt/authz/rd/save";
    public static final String MANAGEMENT_AUTHZ_RD_DELETE = "/v1/mgmt/authz/rd/delete";
    public static final String MANAGEMENT_AUTHZ_RE_CREATE = "/v1/mgmt/authz/re/create";
    public static final String MANAGEMENT_AUTHZ_RE_DELETE = "/v1/mgmt/authz/re/delete";
    public static final String MANAGEMENT_AUTHZ_RE_DELETE_RESOURCES = "/v1/mgmt/authz/re/deleteresources";
    public static final String MANAGEMENT_AUTHZ_RE_HAS_RELATIONS = "/v1/mgmt/authz/re/has";
    public static final String MANAGEMENT_AUTHZ_RE_WHO = "/v1/mgmt/authz/re/who";
    public static final String MANAGEMENT_AUTHZ_RE_RESOURCE = "/v1/mgmt/authz/re/resource";
    public static final String MANAGEMENT_AUTHZ_RE_TARGETS = "/v1/mgmt/authz/re/targets";
    public static final String MANAGEMENT_AUTHZ_RE_TARGET_ALL = "/v1/mgmt/authz/re/targetall";
    public static final String MANAGEMENT_AUTHZ_GET_MODIFIED = "/v1/mgmt/authz/getmodified";

    // Password settings
    public static final String MANAGEMENT_PASSWORD_SETTINGS = "/v1/mgmt/password/settings";
  }
}
