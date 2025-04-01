package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;

public interface OAuthService {

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign up or in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return a string represent the redirect URL
   */
  String start(String provider, String returnURL, LoginOptions loginOptions)
      throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign up or in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param authParams - additional query params to append to the return URL
   * @return a string represent the redirect URL
   */
  String start(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams)
      throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return a string represent the redirect URL
   */
  String startSignIn(String provider, String returnURL, LoginOptions loginOptions)
      throws DescopeException;

  /** 
   * Use to start an OAuth authentication using the given OAuthProvider with sign in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param authParams - additional query params to append to the return URL
   * @return a string represent the redirect URL
   */
  String startSignIn(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams)
      throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign up and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return a string represent the redirect URL
   */
  String startSignUp(String provider, String returnURL, LoginOptions loginOptions)
      throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign up and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param authParams - additional query params to append to the return URL
   * @return a string represent the redirect URL
   */
  String startSignUp(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams)
      throws DescopeException;

  /**
   * Use to exchange the OAuth code with actual {@link AuthenticationInfo}.
   *
   * @param code - Code to be validated
   * @return {@link AuthenticationInfo}
   */
  AuthenticationInfo exchangeToken(String code) throws DescopeException;
}
