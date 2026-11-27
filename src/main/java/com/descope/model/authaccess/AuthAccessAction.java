package com.descope.model.authaccess;

import lombok.experimental.UtilityClass;

/** Values of the {@link AuthAccessRule} effect and the {@link AuthAccessSettings} defaultAction. */
@UtilityClass
public class AuthAccessAction {
  public static final String ALLOW = "allow";
  public static final String BLOCK = "block";
}
