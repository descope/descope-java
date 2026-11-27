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
   * @param prompt         - optional prompts for the OAuth provider, overriding the configuration
   * @param loginHint      - optional login hint for the OAuth provider
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
   * @param prompt         - optional prompts for the OAuth provider, overriding the configuration
   * @param loginHint      - optional login hint for the OAuth provider
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
   * @param prompt         - optional prompts for the OAuth provider, overriding the configuration
   * @param loginHint      - optional login hint for the OAuth provider
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
   * @param refreshToken  - the user's refresh token
   * @param loginOptions  - {@link LoginOptions loginOptions}
   * @param allowAllMerge - update the user even when it shares no identifier, such as email, with the OAuth user
   * @return a string represent the redirect URL
   */
  String updateUser(String provider, String returnURL, String refreshToken, LoginOptions loginOptions,
      boolean allowAllMerge) throws DescopeException;

  /**
   * Use to start an OAuth flow that adds the given OAuthProvider to an existing, signed in user.
   *
   * @param provider       - provider
   * @param returnURL      - return url
   * @param refreshToken   - the user's refresh token
   * @param loginOptions   - {@link LoginOptions loginOptions}
   * @param allowAllMerge  - update the user even when it shares no identifier, such as email, with the OAuth user
   * @param prompt         - optional prompts for the OAuth provider, overriding the configuration
   * @param loginHint      - optional login hint for the OAuth provider
   * @param initiatedEmail - optional email of the user who initiated the authentication
   * @return a string represent the redirect URL
   */
  String updateUser(String provider, String returnURL, String refreshToken, LoginOptions loginOptions,
      boolean allowAllMerge, List<OAuthPrompt> prompt, String loginHint, String initiatedEmail)
      throws DescopeException;

  /**
   * Use to exchange the OAuth code with actual {@link AuthenticationInfo}.
   *
   * @param code - Code to be validated
   * @return {@link AuthenticationInfo}. When {@code getSsoAuthenticationOnly()} is true,
   *     no session was issued and the token is null: do not treat the result as a login.
   */
  AuthenticationInfo exchangeToken(String code) throws DescopeException;
}