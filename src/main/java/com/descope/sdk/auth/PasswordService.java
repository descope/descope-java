package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.password.PasswordPolicy;
import com.descope.model.user.User;

public interface PasswordService {

  AuthenticationInfo signUp(
      String loginId,
      User user,
      String password)
      throws DescopeException;

  AuthenticationInfo signIn(
      String loginId,
      String password) throws DescopeException;

  void sendPasswordReset(
      String loginId,
      String redirectURL) throws DescopeException;

  void updateUserPassword(
      String loginId,
      String newPassword) throws DescopeException;

  void replaceUserPassword(
      String loginId,
      String oldPassword,
      String newPassword) throws DescopeException;

  PasswordPolicy getPasswordPolicy() throws DescopeException;
}
