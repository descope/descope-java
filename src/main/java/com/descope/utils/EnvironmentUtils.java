package com.descope.utils;

import static com.descope.literals.AppConstants.MANAGEMENT_KEY_ENV_VAR;
import static com.descope.literals.AppConstants.PROJECT_ID_ENV_VAR;
import static com.descope.literals.AppConstants.PUBLIC_KEY_ENV_VAR;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EnvironmentUtils {
  private static Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();

  public static String getProjectId() {
    return dotenv.get(PROJECT_ID_ENV_VAR);
  }

  public static String getPublicKey() {
    return dotenv.get(PUBLIC_KEY_ENV_VAR);
  }

  public static String getManagementKey() {
    return dotenv.get(MANAGEMENT_KEY_ENV_VAR);
  }
}
