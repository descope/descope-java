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
   * ExchangeToken - Finalize SAML authentication.
   *
   * @param code - Code to be validated
   * @return Authentication info
   * @throws DescopeException - error upon failure
   */

  AuthenticationInfo exchangeToken(String code) throws DescopeException;

}
