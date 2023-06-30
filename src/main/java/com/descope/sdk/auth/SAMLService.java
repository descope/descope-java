package com.descope.sdk.auth;

import com.descope.exception.DescopeException;
import com.descope.model.auth.AuthenticationInfo;
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
   * ExchangeToken - Finalize SAML authentication.
   *
   * @param code - Code to be validated
   * @return Authentication info
   * @throws DescopeException - error upon failure
   */

  AuthenticationInfo exchangeToken(String code) throws DescopeException;

}
