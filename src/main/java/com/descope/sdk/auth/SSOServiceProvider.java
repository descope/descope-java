package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.magiclink.LoginOptions;

public interface SSOServiceProvider {
  /**
   * Start will initiate an SSO login flow.
   *
   * @param tenant       - tenant
   * @param redirectUrl - URL to redirect user to overriding configuration
   * @param prompt - Prompt to the user overriding configuration
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return will be the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String redirectUrl, String prompt, LoginOptions loginOptions) throws DescopeException;

  /**
   * Start will initiate an SSO login flow.
   *
   * @param tenant       - tenant
   * @param redirectUrl - URL to redirect user to overriding configuration
   * @param prompt - Prompt to the user overriding configuration
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param refreshToken - if we are doing step-up or MFA, existing refresh token is required
   * @return will be the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String redirectUrl, String prompt, LoginOptions loginOptions, String refreshToken)
      throws DescopeException;

  /**
   * Start an SSO login flow with an optional SSO connection and login hint.
   *
   * @param tenant - tenant
   * @param redirectUrl - URL to redirect user to overriding configuration
   * @param prompt - prompt to the user overriding configuration
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param ssoId - optional SSO connection ID
   * @param loginHint - optional login hint for the identity provider
   * @return the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String redirectUrl, String prompt, LoginOptions loginOptions, String ssoId,
      String loginHint) throws DescopeException;

  /**
   * Start an SSO login flow with an optional SSO connection, login hint, and refresh token.
   *
   * @param tenant - tenant
   * @param redirectUrl - URL to redirect user to overriding configuration
   * @param prompt - prompt to the user overriding configuration
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param refreshToken - existing refresh token for step-up or MFA
   * @param ssoId - optional SSO connection ID
   * @param loginHint - optional login hint for the identity provider
   * @return the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String redirectUrl, String prompt, LoginOptions loginOptions, String refreshToken,
      String ssoId, String loginHint) throws DescopeException;

  /**
   * ExchangeToken - Finalize SAML/OIDC SSO authentication.
   *
   * @param code - Code to be validated
   * @return Authentication info
   * @throws DescopeException - error upon failure
   */

  AuthenticationInfo exchangeToken(String code) throws DescopeException;

}
