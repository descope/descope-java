package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
import com.descope.model.auth.SAMLResponse;
import com.descope.model.auth.SAMLStartOptions;
import com.descope.model.magiclink.LoginOptions;

public interface SAMLService {
  /**
   * tart will initiate a SAML login flow.
   *
   * @param tenant       - tenant
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @return will be the redirect URL that needs to return to client
   * @throws DescopeException - error upon failure
   */
  String start(String tenant, String returnURL, LoginOptions loginOptions) throws DescopeException;

  /**
   * Start will initiate a SAML login flow with additional options.
   *
   * @param tenant       - tenant
   * @param returnURL    - return url
   * @param loginOptions - {@link LoginOptions loginOptions}
   * @param options      - {@link SAMLStartOptions options}, may be null
   * @return the redirect URL that needs to return to client, and an error ID if the server returned one
   * @throws DescopeException - error upon failure
   */
  SAMLResponse start(String tenant, String returnURL, LoginOptions loginOptions, SAMLStartOptions options)
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
