package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthResponse;
import com.descope.model.auth.OAuthStartOptions;
import com.descope.model.magiclink.LoginOptions;
import java.util.Map;

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
   * @param authParams - additional query params to append to the authorize URL
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
   * @param authParams - additional query params to append to the authorize URL
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
   * @param authParams - additional query params to append to the authorize URL
   * @return a string represent the redirect URL
   */
  String startSignUp(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams)
      throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign up or in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param options      - {@link OAuthStartOptions options}, may be null
   * @return {@link OAuthResponse} with the redirect URL and, on error, the error ID
   */
  OAuthResponse startWithOptions(String provider, String returnURL, LoginOptions loginOptions,
      OAuthStartOptions options) throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param options      - {@link OAuthStartOptions options}, may be null
   * @return {@link OAuthResponse} with the redirect URL and, on error, the error ID
   */
  OAuthResponse startSignInWithOptions(String provider, String returnURL, LoginOptions loginOptions,
      OAuthStartOptions options) throws DescopeException;

  /**
   * Use to start an OAuth authentication using the given OAuthProvider with sign up and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param options      - {@link OAuthStartOptions options}, may be null
   * @return {@link OAuthResponse} with the redirect URL and, on error, the error ID
   */
  OAuthResponse startSignUpWithOptions(String provider, String returnURL, LoginOptions loginOptions,
      OAuthStartOptions options) throws DescopeException;

  /**
   * Use to add an OAuth identity to an existing, signed-in user.
   *
   * @param provider      - provider
   * @param returnURL     - return url
   * @param refreshToken  - the user's refresh token
   * @param allowAllMerge - link the identity even when the provider's email is not one of the user's login IDs,
   *                        may be null
   * @param loginOptions  - {@link LoginOptions loginOptions}
   * @param options       - {@link OAuthStartOptions options}, may be null; rawResponse is not supported
   * @return {@link OAuthResponse} with the redirect URL and, on error, the error ID
   */
  OAuthResponse updateUser(String provider, String returnURL, String refreshToken, Boolean allowAllMerge,
      LoginOptions loginOptions, OAuthStartOptions options) throws DescopeException;

  /**
   * Use to exchange the OAuth code with actual {@link AuthenticationInfo}.
   *
   * @param code - Code to be validated
   * @return {@link AuthenticationInfo}
   */
  AuthenticationInfo exchangeToken(String code) throws DescopeException;
}