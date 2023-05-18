package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;

public interface OAuthService {

  /**
   * Use to start an OAuth authentication using the given OAuthProvider
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return a string represent the redirect URL
   */
  String start(String provider, String returnURL, LoginOptions loginOptions) throws DescopeException;

  /**
   * @param code - Code to be validated
   * @return Authentication info
   */
  AuthenticationInfo exchangeToken(String code) throws DescopeException;
}
