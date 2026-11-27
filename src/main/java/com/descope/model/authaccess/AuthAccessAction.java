package com.descope.model.authaccess;

import lombok.experimental.UtilityClass;

/** Values of {@link AuthAccessRule#getEffect()} and {@link AuthAccessSettings#getDefaultAction()}. */
@UtilityClass
public class AuthAccessAction {
  public static final String ALLOW = "allow";
  public static final String BLOCK = "block";
}
