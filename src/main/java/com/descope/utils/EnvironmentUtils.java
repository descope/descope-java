package com.descope.utils;

import static com.descope.literals.AppConstants.MANAGEMENT_KEY_ENV_VAR;
import static com.descope.literals.AppConstants.PROJECT_ID_ENV_VAR;
import static com.descope.literals.AppConstants.PUBLIC_KEY_ENV_VAR;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvironmentUtils {

  public static String getProjectId() {
    return System.getenv(PROJECT_ID_ENV_VAR);
  }

  public static String getPublicKey() {
    return System.getenv(PUBLIC_KEY_ENV_VAR);
  }

  public static String getManagementKey() {
    return System.getenv(MANAGEMENT_KEY_ENV_VAR);
  }
}
