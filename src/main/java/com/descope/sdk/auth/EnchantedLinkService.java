package com.descope.sdk.auth;

import com.descope.enums.DeliveryMethod;
import com.descope.exception.DescopeException;
import com.descope.model.User;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;

public interface EnchantedLinkService {
  String signIn(
      DeliveryMethod deliveryMethod,
      String loginId,
      String uri,
      LoginOptions loginOptions)
      throws DescopeException;

  String signUp(String loginId, String uri, User user)
      throws DescopeException;

  String signUpOrIn(DeliveryMethod deliveryMethod, String loginId, String uri)
      throws DescopeException;

  AuthenticationInfo getSession(String pendingRef) throws DescopeException;

  AuthenticationInfo verify(String token) throws DescopeException;

  String updateUserEmail(String loginId, String email, String uri)
      throws DescopeException;

}