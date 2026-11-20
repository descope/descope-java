package com.descope.sdk.auth;

import com.descope.enums.OAuthPrompt;
import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;
import java.util.List;
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
   * @param provider       - provider
   * @param returnURL      - return url
   * @param loginOptions   - {@link LoginOptions loginOptions}
   * @param authParams     - additional query params to append to the authorize URL
   * @param prompt         - optional prompt values to send to the provider
   * @param loginHint      - optional hint of the user's login ID to send to the provider
   * @param initiatedEmail - optional email of the user who initiated the authentication
   * @return a string represent the redirect URL
   */
  String start(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams,
      List<OAuthPrompt> prompt, String loginHint, String initiatedEmail) throws DescopeException;

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
   * @param provider       - provider
   * @param returnURL      - return url
   * @param loginOptions   - {@link LoginOptions loginOptions}
   * @param authParams     - additional query params to append to the authorize URL
   * @param prompt         - optional prompt values to send to the provider
   * @param loginHint      - optional hint of the user's login ID to send to the provider
   * @param initiatedEmail - optional email of the user who initiated the authentication
   * @return a string represent the redirect URL
   */
  String startSignIn(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams,
      List<OAuthPrompt> prompt, String loginHint, String initiatedEmail) throws DescopeException;

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
   * @param provider       - provider
   * @param returnURL      - return url
   * @param loginOptions   - {@link LoginOptions loginOptions}
   * @param authParams     - additional query params to append to the authorize URL
   * @param prompt         - optional prompt values to send to the provider
   * @param loginHint      - optional hint of the user's login ID to send to the provider
   * @param initiatedEmail - optional email of the user who initiated the authentication
   * @return a string represent the redirect URL
   */
  String startSignUp(String provider, String returnURL, LoginOptions loginOptions, Map<String, String> authParams,
      List<OAuthPrompt> prompt, String loginHint, String initiatedEmail) throws DescopeException;

  /**
   * Use to start an OAuth flow that adds the given OAuthProvider to an existing, signed in user.
   *
   * @param provider      - provider
   * @param returnURL     - return url
   * @param refreshToken  - the signed in user's refresh token
   * @param allowAllMerge - whether to allow all user merges
   * @param loginOptions  - {@link LoginOptions loginOptions}
   * @return a string represent the redirect URL
   */
  String updateUser(String provider, String returnURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions) throws DescopeException;

  /**
   * Use to start an OAuth flow that adds the given OAuthProvider to an existing, signed in user.
   *
   * @param provider       - provider
   * @param returnURL      - return url
   * @param refreshToken   - the signed in user's refresh token
   * @param allowAllMerge  - whether to allow all user merges
   * @param loginOptions   - {@link LoginOptions loginOptions}
   * @param prompt         - optional prompt values to send to the provider
   * @param loginHint      - optional hint of the user's login ID to send to the provider
   * @param initiatedEmail - optional email of the user who initiated the authentication
   * @return a string represent the redirect URL
   */
  String updateUser(String provider, String returnURL, String refreshToken, boolean allowAllMerge,
      LoginOptions loginOptions, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException;

  /**
   * Use to exchange the OAuth code with actual {@link AuthenticationInfo}.
   *
   * @param code - Code to be validated
   * @return {@link AuthenticationInfo}
   */
  AuthenticationInfo exchangeToken(String code) throws DescopeException;
}