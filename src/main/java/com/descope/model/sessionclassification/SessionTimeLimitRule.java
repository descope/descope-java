package com.descope.model.sessionclassification;

/** Values for {@link SessionClassification#getSessionTimeLimitRule()}. */
public final class SessionTimeLimitRule {
  /** The classification does not override the limit, like a user with no classification. */
  public static final String UNSPECIFIED = "SESSION_TIME_LIMIT_RULE_UNSPECIFIED";
  /** The shortest limit among all the tenants the user belongs to. */
  public static final String MIN_OF_TENANTS = "SESSION_TIME_LIMIT_RULE_MIN_OF_TENANTS";
  /** The project-wide session time limit. */
  public static final String PROJECT_SETTINGS = "SESSION_TIME_LIMIT_RULE_PROJECT_SETTINGS";
  /** The limit of the tenant the session is currently in. */
  public static final String CURRENT_TENANT = "SESSION_TIME_LIMIT_RULE_CURRENT_TENANT";

  private SessionTimeLimitRule() {}
}
