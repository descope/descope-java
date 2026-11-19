package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.OAuthAuthorizeOptions;
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
   * Use to start an OAuth authentication using the given OAuthProvider with sign up or in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}; step-up and MFA require its refresh JWT in jwt
   * @param authParams - additional query params to append to the authorize URL
   * @param options - {@link OAuthAuthorizeOptions options} such as prompt and login hint
   * @return a string represent the redirect URL
   */
  String start(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams,
      OAuthAuthorizeOptions options) throws DescopeException;

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
   * Use to start an OAuth authentication using the given OAuthProvider with sign in and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}; step-up and MFA require its refresh JWT in jwt
   * @param authParams - additional query params to append to the authorize URL
   * @param options - {@link OAuthAuthorizeOptions options} such as prompt and login hint
   * @return a string represent the redirect URL
   */
  String startSignIn(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams,
      OAuthAuthorizeOptions options) throws DescopeException;

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
   * Use to start an OAuth authentication using the given OAuthProvider with sign up and options.
   *
   * @param provider     - provider
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}; step-up and MFA require its refresh JWT in jwt
   * @param authParams - additional query params to append to the authorize URL
   * @param options - {@link OAuthAuthorizeOptions options} such as prompt and login hint
   * @return a string represent the redirect URL
   */
  String startSignUp(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams,
      OAuthAuthorizeOptions options) throws DescopeException;

  /**
   * Use to exchange the OAuth code with actual {@link AuthenticationInfo}.
   *
   * @param code - Code to be validated
   * @return {@link AuthenticationInfo}
   */
  AuthenticationInfo exchangeToken(String code) throws DescopeException;

  /**
   * Use to start an OAuth authentication that adds the given OAuthProvider to the logged-in user.
   *
   * @param provider      - provider
   * @param returnURL     - return url
   * @param refreshToken  - the logged-in user's refresh token
   * @param allowAllMerge - add the provider even when its email isn't one of the user's login IDs
   * @param loginOptions  - {@link LoginOptions loginOptions}
   * @return a string represent the redirect URL
   */
  String updateUser(String provider, String returnURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions) throws DescopeException;

  /**
   * Use to start an OAuth authentication that adds the given OAuthProvider to the logged-in user.
   *
   * @param provider      - provider
   * @param returnURL     - return url
   * @param refreshToken  - the logged-in user's refresh token
   * @param allowAllMerge - add the provider even when its email isn't one of the user's login IDs
   * @param loginOptions  - {@link LoginOptions loginOptions}
   * @param options       - {@link OAuthAuthorizeOptions options} such as prompt and login hint
   * @return a string represent the redirect URL
   */
  String updateUser(String provider, String returnURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions, OAuthAuthorizeOptions options) throws DescopeException;
}