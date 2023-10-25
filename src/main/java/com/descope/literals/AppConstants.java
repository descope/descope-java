package com.descope.literals;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {
  public static final String PROJECT_ID_ENV_VAR = "DESCOPE_PROJECT_ID";
  public static final String PUBLIC_KEY_ENV_VAR = "DESCOPE_PUBLIC_KEY";
  public static final String MANAGEMENT_KEY_ENV_VAR = "DESCOPE_MANAGEMENT_KEY";
  public static final String BASE_URL_ENV_VAR = "DESCOPE_BASE_URL";
  public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
  public static final String BEARER_AUTHORIZATION_PREFIX = "Bearer ";
  public static final String COOKIE = "Cookie";
  public static final String SESSION_COOKIE_NAME = "DS";
  public static final String REFRESH_COOKIE_NAME = "DSR";
  public static final String TENANTS_CLAIM_KEY = "tenants";
  public static final String PERMISSIONS_CLAIM_KEY = "permissions";
  public static final String ROLES_CLAIM_KEY = "roles";
  public static final String OAUTH_PROVIDER_APPLE = "apple";
  public static final String OAUTH_PROVIDER_DISCORD = "discord";
  public static final String OAUTH_PROVIDER_FACEBOOK = "facebook";
  public static final String OAUTH_PROVIDER_GITHUB = "github";
  public static final String OAUTH_PROVIDER_GITLAB = "gitlab";
  public static final String OAUTH_PROVIDER_GOOGLE = "google";
  public static final String OAUTH_PROVIDER_LINKEDIN = "linkedin";
  public static final String OAUTH_PROVIDER_MICROSOFT = "microsoft";
  public static final String OAUTH_PROVIDER_SLACK = "slack";
}
